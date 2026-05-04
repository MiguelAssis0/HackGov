package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.Approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.DTOs.PageResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.RequisitionDTO;
import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.mappers.PageMapper;
import com.fiap.hackgov.bidding.internal.mappers.RequisitionMapper;
import com.fiap.hackgov.bidding.internal.services.RequisitionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/requisition")
public class RequisitionController {

    @Autowired
    private RequisitionService requisitionService;

    @Autowired
    private RequisitionMapper requisitionMapper;

    @Autowired
    private PageMapper pageMapper;


    @GetMapping
    public ResponseEntity<PageResponseDTO<RequisitionDTO>> getAllRequisitions(Pageable pageable) {
        Page<Requisition> requisitions = requisitionService.findAll(pageable);
        return ResponseEntity.ok(pageMapper.toPageResponseDto(requisitions, requisitionMapper::toDTO));
    }

    @PostMapping
    public ResponseEntity<?> createRequisition(@RequestBody @Valid CreateRequisitionDTO createRequisitionDTO) {
        Requisition requisition = requisitionService.save(createRequisitionDTO);
        URI uri = URI.create("/api/requisition/" + requisition.getId());
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/approvals")
    public ResponseEntity<?> addApproval(
            @PathVariable UUID id,
            @RequestBody @Valid CreateApprovalDTO approvalDTO
    ) {

        Approval approval = requisitionService.addApproval(id, approvalDTO);
        URI uri = URI.create("/api/requisition/" + approval.getId());
        return ResponseEntity.created(uri).build();
    }
}
