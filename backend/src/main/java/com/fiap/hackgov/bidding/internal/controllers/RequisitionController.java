package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.RequisitionResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.mappers.RequisitionMapper;
import com.fiap.hackgov.bidding.internal.services.RequisitionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/requisition")
public class RequisitionController {

    @Autowired
    private RequisitionService requisitionService;

    @Autowired
    private RequisitionMapper requisitionMapper;


    @GetMapping
    public ResponseEntity<Page<RequisitionResponseDTO>> getAllRequisitions(
            Pageable pageable
    ) {

        Page<Requisition> requisitions =
                requisitionService.findAll(pageable);

        Page<RequisitionResponseDTO> response =
                requisitions.map(requisitionMapper::toDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createRequisition(@RequestBody @Valid CreateRequisitionDTO createRequisitionDTO) {
        Requisition requisition = requisitionService.save(createRequisitionDTO);
        URI uri = URI.create("/api/requisition/" + requisition.getId());
        return ResponseEntity.created(uri).build();
    }

}
