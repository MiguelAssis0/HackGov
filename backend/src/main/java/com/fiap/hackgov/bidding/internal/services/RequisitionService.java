package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.entities.ETP;
import com.fiap.hackgov.bidding.internal.entities.ProcessState;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalSector;
import com.fiap.hackgov.bidding.internal.entities.enums.ApprovalStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;
import com.fiap.hackgov.bidding.internal.mappers.RequisitionMapper;
import com.fiap.hackgov.bidding.internal.repositories.ApprovalRepository;
import com.fiap.hackgov.bidding.internal.repositories.ETPRepository;
import com.fiap.hackgov.bidding.internal.repositories.ProcessStateRepository;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RequisitionService {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private ProcessStateRepository processStateRepository;

    @Autowired
    private ETPRepository etpRepository;

    @Autowired
    private RequisitionMapper requisitionMapper;

    public Page<Requisition> findAll(Pageable pageable) {
        return requisitionRepository.findAll(pageable);
    }

    public Requisition findById(UUID id) {
        return requisitionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Requisition not found:" + id));
    }

    @Transactional
    public Requisition save(CreateRequisitionDTO createRequisitionDTO) {

        Requisition requisition =
                requisitionMapper.toEntity(createRequisitionDTO);

        requisition.setRequestStatus(RequestStatus.CADASTRADA);
        requisition.setNumber(generateRequisitionNumber());

        requisition = requisitionRepository.save(requisition);

        if (createRequisitionDTO.requiresEtp()) {

            ETP etp = new ETP();

            etp.setRequisition(requisition);
            etp.setContent(requisition.getTechnicalDescription());

            etp = etpRepository.save(etp);

            requisition.setEtp(etp);
        }

        List<Approval> approvals = new ArrayList<>();

        for (ApprovalSector sector : ApprovalSector.values()) {

            Approval approval = new Approval();

            approval.setRequisition(requisition);
            approval.setApprovalSector(sector);
            approval.setApprovalStatus(ApprovalStatus.PENDENTE);

            approvals.add(approval);
        }

        approvalRepository.saveAll(approvals);

        requisition.setApprovals(approvals);

        ProcessState processState = new ProcessState();

        processState.setBiddingProcess(requisition);
        processState.setCurrentStage(ProcessStage.REQUISICAO_CADASTRADA);
        processState.setNumberStep(1);
        processState.setStartedAt(LocalDateTime.now());
        processState.setObservation("Requisição cadastrada no sistema");

        processState = processStateRepository.save(processState);

        requisition.setProcessState(processState);

        return requisitionRepository.save(requisition);
    }

    public String generateRequisitionNumber() {

        String year = String.valueOf(LocalDate.now().getYear());

        List<Requisition> requisitions =
                requisitionRepository.findLastRequisitionNumber(
                        year,
                        PageRequest.of(0, 1)
                );

        int nextNumber = 1;

        if (!requisitions.isEmpty()) {

            String lastNumber = requisitions.get(0).getNumber();

            String numericPart =
                    lastNumber.substring(lastNumber.lastIndexOf("-") + 1);

            nextNumber = Integer.parseInt(numericPart) + 1;
        }

        return String.format("REQ-%s-%06d", year, nextNumber);
    }

}
