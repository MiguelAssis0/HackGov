package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.Approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.entities.ETP;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.RequestStatus;
import com.fiap.hackgov.bidding.internal.mappers.ApprovalMapper;
import com.fiap.hackgov.bidding.internal.mappers.RequisitionMapper;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
import com.fiap.hackgov.shared.infra.exceptions.ResourceAlreadyExistsException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class RequisitionService {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private ETPService etpService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private RequisitionMapper requisitionMapper;

    @Autowired
    private ApprovalMapper approvalMapper;

    public Page<Requisition> findAll(Pageable pageable) {
        return requisitionRepository.findAll(pageable);
    }

    public Requisition findById(UUID id) {
        return requisitionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Requisition not found:" + id));
    }

    public Requisition save(CreateRequisitionDTO createRequisitionDTO) {
        Requisition requisition = requisitionRepository.save(requisitionMapper.toEntity(createRequisitionDTO));
        requisition.setRequestStatus(RequestStatus.CADASTRADA);
        if (createRequisitionDTO.requiresEtp()){
            ETP etp = new ETP();
            etp.setRequisition(requisition);
            etp.setContent(requisition.getTechnicianDescription());
            etpService.save(etp);
        }
        return requisition;
    }

    public Approval addApproval(UUID id, CreateApprovalDTO createApprovalDTO){

        Requisition requisition = findById(id);

        boolean alreadyApproved = requisition.getApprovals()
                .stream()
                .anyMatch(a -> a.getStage() == createApprovalDTO.stage());

        if (alreadyApproved) {
            throw new ResourceAlreadyExistsException("This stage has already been:" + createApprovalDTO.stage());
        }

        Approval approval = approvalMapper.toEntity(createApprovalDTO);

        approval.setRequisition(requisition);
        approval.setApprovedAt(LocalDateTime.now());
        approvalService.save(approval);
        requisition.getApprovals().add(approval);
        requisitionRepository.save(requisition);
        return approval;
    }
}
