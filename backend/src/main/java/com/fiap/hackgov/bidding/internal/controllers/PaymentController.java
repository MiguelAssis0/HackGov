package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.payment.CreatePaymentDTO;
import com.fiap.hackgov.bidding.internal.DTOs.payment.PaymentResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Payment;
import com.fiap.hackgov.bidding.internal.mappers.PaymentMapper;
import com.fiap.hackgov.bidding.internal.services.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final PaginationMapper paginationMapper;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> create(@Valid @RequestBody CreatePaymentDTO dto) {
        Payment payment = paymentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMapper.toDTO(payment));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<PaymentResponseDTO>> findAll(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponseDTO> dtoPage = paymentService.findAll(pageable).map(paymentMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentMapper.toDTO(paymentService.findById(id)));
    }

    @GetMapping("/declaration/{declarationId}")
    public ResponseEntity<PageResponseDTO<PaymentResponseDTO>> findByDeclarationId(
            @PathVariable UUID declarationId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PaymentResponseDTO> dtoPage = paymentService.findByDeclarationId(declarationId, pageable).map(paymentMapper::toDTO);
        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }
}
