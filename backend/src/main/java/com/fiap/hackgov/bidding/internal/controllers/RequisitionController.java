package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.processHistory.ProcessHistoryDTO;
import com.fiap.hackgov.bidding.internal.DTOs.processStatus.AdvanceRequisitionStageDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.AssignProcurementResponsibleDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.RequisitionResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.RequisitionResponsibleDTO;
import com.fiap.hackgov.bidding.internal.services.RequisitionService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.Response;
import com.fiap.hackgov.documents.internal.services.MunicipalDocumentService;
import com.fiap.hackgov.shared.infra.pagination.PageResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requisitions")
public class RequisitionController {

    @Autowired
    private RequisitionService requisitionService;

    @Autowired
    private MunicipalDocumentService documentService;

    @GetMapping
    public PageResponseDTO<RequisitionResponseDTO> findAll(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return requisitionService.findAll(pageable);
    }


    @GetMapping("/{id}/history")
    public ResponseEntity<List<ProcessHistoryDTO>> getHistory(@PathVariable UUID id) {

        return ResponseEntity.ok(requisitionService.getHistory(id));
    }

    @GetMapping("/{id}/procurement-employees")
    public ResponseEntity<List<RequisitionResponsibleDTO>> getProcurementEmployees(@PathVariable UUID id) {
        return ResponseEntity.ok(requisitionService.findProcurementEmployees(id));
    }

    @PostMapping
    public ResponseEntity<RequisitionResponseDTO> create(
            @AuthenticationPrincipal Employee employee,
            @Valid @RequestBody CreateRequisitionDTO dto
    ) {

        RequisitionResponseDTO requisition = requisitionService.create(employee, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(requisition);
    }

    @GetMapping("/{id}/documents")
    public List<Response> documents(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        requisitionService.findById(id);
        return documentService.listForProcess(id, employee);
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> uploadDocument(@PathVariable UUID id,
                                                   @RequestParam String title,
                                                   @RequestParam(defaultValue = "PROCESS") String documentType,
                                                   @RequestParam(defaultValue = "") String description,
                                                   @RequestPart MultipartFile file,
                                                   @AuthenticationPrincipal Employee employee) {
        requisitionService.findById(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadForProcess(id, title, documentType, description, file, employee));
    }

    @PatchMapping("/{id}/advance-stage")
    public ResponseEntity<RequisitionResponseDTO> advanceStage(@PathVariable UUID id, @Valid @RequestBody AdvanceRequisitionStageDTO dto, @AuthenticationPrincipal Employee employee) {

        RequisitionResponseDTO response = requisitionService.advanceStage(id, dto, employee);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/procurement-responsible")
    public ResponseEntity<RequisitionResponseDTO> assignProcurementResponsible(
            @PathVariable UUID id,
            @Valid @RequestBody AssignProcurementResponsibleDTO dto,
            @AuthenticationPrincipal Employee employee
    ) {
        return ResponseEntity.ok(requisitionService.assignProcurementResponsible(id, dto, employee));
    }
}
