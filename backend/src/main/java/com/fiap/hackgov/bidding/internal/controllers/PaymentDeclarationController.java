package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.paymentDeclaration.CreatePaymentDeclarationDTO;
import com.fiap.hackgov.bidding.internal.DTOs.paymentDeclaration.PaymentDeclarationResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.PaymentDeclaration;
import com.fiap.hackgov.bidding.internal.mappers.PaymentDeclarationMapper;
import com.fiap.hackgov.bidding.internal.services.PaymentDeclarationService;
import com.fiap.hackgov.shared.infra.pagination.PageResponseDTO;
import com.fiap.hackgov.shared.infra.pagination.PaginationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment-declarations")
@RequiredArgsConstructor
public class PaymentDeclarationController {

    private final PaymentDeclarationService paymentDeclarationService;
    private final PaymentDeclarationMapper paymentDeclarationMapper;
    private final PaginationMapper paginationMapper;

    @PostMapping
    public ResponseEntity<PaymentDeclarationResponseDTO> create(@Valid @RequestBody CreatePaymentDeclarationDTO dto) {
        PaymentDeclaration declaration = paymentDeclarationService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentDeclarationMapper.toDTO(declaration));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<PaymentDeclarationResponseDTO>> findAll(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentDeclarationResponseDTO> dtoPage = paymentDeclarationService.findAll(pageable).map(paymentDeclarationMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDeclarationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentDeclarationMapper.toDTO(paymentDeclarationService.findById(id)));
    }

    @GetMapping("/commitment/{commitmentId}")
    public ResponseEntity<PageResponseDTO<PaymentDeclarationResponseDTO>> findByCommitmentId(
            @PathVariable UUID commitmentId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PaymentDeclarationResponseDTO> dtoPage = paymentDeclarationService.findByCommitmentId(commitmentId, pageable).map(paymentDeclarationMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }
}
