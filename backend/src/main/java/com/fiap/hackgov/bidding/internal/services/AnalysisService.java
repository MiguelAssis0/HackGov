package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.analysis.AnalysisResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.analysis.CreateAnalysisDTO;
import com.fiap.hackgov.bidding.internal.DTOs.analysis.UpdateAnalysisDTO;
import com.fiap.hackgov.bidding.internal.entities.Analysis;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.AnalysisResult;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.mappers.AnalysisMapper;
import com.fiap.hackgov.bidding.internal.repositories.AnalysisRepository;
import com.fiap.hackgov.bidding.internal.repositories.RequisitionRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final RequisitionRepository requisitionRepository;
    private final RequisitionService requisitionService;
    private final ProcessHistoryService processHistoryService;
    private final AnalysisMapper analysisMapper;

    public AnalysisResponseDTO create(CreateAnalysisDTO dto) {
        Requisition requisition = requisitionRepository.findById(dto.requisitionId())
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + dto.requisitionId()));

        Analysis analysis = analysisMapper.toEntity(dto);
        analysis.setRequisition(requisition);
        analysis.setResult(AnalysisResult.PENDENTE);
        analysis = analysisRepository.save(analysis);

        return analysisMapper.toDTO(analysis);
    }

    public Page<AnalysisResponseDTO> findAll(Pageable pageable) {
        return analysisRepository.findAll(pageable).map(analysisMapper::toDTO);
    }

    public Page<AnalysisResponseDTO> findPending(Pageable pageable) {
        return analysisRepository.findByResult(AnalysisResult.PENDENTE, pageable).map(analysisMapper::toDTO);
    }

    public AnalysisResponseDTO findById(UUID id) {
        Analysis analysis = findEntityById(id);

        return analysisMapper.toDTO(analysis);
    }

    public AnalysisResponseDTO processAnalysis(UUID id, UpdateAnalysisDTO dto, Employee employee) {
        Analysis analysis = findEntityById(id);

        analysis.setResult(dto.result());
        analysis.setAnalyzedBy(employee);
        analysis.setObservation(dto.observation());
        analysis.setAnalyzedAt(LocalDateTime.now());

        analysis = analysisRepository.save(analysis);

        Requisition requisition = analysis.getRequisition();
        ProcessStage currentStage = requisition.getProcessStatus().getStage();

        if (currentStage != analysis.getStage()) {
            throw new BusinessException("Analysis does not belong to the current requisition stage");
        }

        switch (dto.result()) {
            case APROVADO -> {
                processHistoryService.createProcessHistory(requisition, employee, "Análise aprovada: " + currentStage.getDescription(), currentStage, HistoryEventType.APPROVED);

                ProcessStage nextStage = getNextStage(currentStage);

                requisitionService.sendToNextStage(requisition, nextStage, employee, "Processo enviado para " + nextStage.getDescription());
            }
            case CORRECAO_NECESSARIA -> {
                processHistoryService.createProcessHistory(requisition, employee, "Correção solicitada na análise: " + currentStage.getDescription(), currentStage, HistoryEventType.REJECTED);

                requisitionService.returnToInitialStage(
                        requisition,
                        employee,
                        "Requisição retornada para " + ProcessStage.REQUISICAO_CADASTRADA.getDescription() + " para correção"
                );
            }
            case REPROVADO, CANCELADO -> processHistoryService.createProcessHistory(requisition, employee, "Análise encerrada: " + currentStage.getDescription(), currentStage, HistoryEventType.REJECTED);
            case PENDENTE -> {
            }
        }

        return analysisMapper.toDTO(analysis);
    }

    public void delete(UUID id) {
        Analysis analysis = findEntityById(id);
        analysisRepository.delete(analysis);
    }

    private Analysis findEntityById(UUID id) {
        return analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
    }

    private ProcessStage getNextStage(ProcessStage currentStage) {
        return Arrays.stream(ProcessStage.values())
                .filter(stage -> stage.getStep() == currentStage.getStep() + 1)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Next stage not found"));
    }
}
