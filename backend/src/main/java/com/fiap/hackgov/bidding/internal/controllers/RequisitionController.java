package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.processHistory.ProcessHistoryDTO;
import com.fiap.hackgov.bidding.internal.DTOs.processStatus.AdvanceRequisitionStageDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.requisiton.RequisitionResponseDTO;
import com.fiap.hackgov.bidding.internal.services.RequisitionService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.pagination.PageResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requisitions")
public class RequisitionController {

    @Autowired
    private RequisitionService requisitionService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<RequisitionResponseDTO>> getAllRequisitions(Pageable pageable) {

        return ResponseEntity.ok(requisitionService.findAll(pageable));
    }


    @GetMapping("/{id}/history")
    public ResponseEntity<List<ProcessHistoryDTO>> getHistory(@PathVariable UUID id) {

        return ResponseEntity.ok(requisitionService.getHistory(id));
    }

    @PostMapping
    public ResponseEntity<RequisitionResponseDTO> create(@AuthenticationPrincipal Employee employee, @RequestBody CreateRequisitionDTO dto) {

        RequisitionResponseDTO requisition = requisitionService.create(employee, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(requisition);
    }

    @PatchMapping("/{id}/advance-stage")
    public ResponseEntity<RequisitionResponseDTO> advanceStage(@PathVariable UUID id, @Valid @RequestBody AdvanceRequisitionStageDTO dto, @AuthenticationPrincipal Employee employee) {

        RequisitionResponseDTO response = requisitionService.advanceStage(id, dto, employee);

        return ResponseEntity.ok(response);
    }
}
