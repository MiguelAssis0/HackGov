package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.PaymentStatement.*;
import com.fiap.hackgov.bidding.internal.services.PaymentStatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-statement")
@RequiredArgsConstructor
public class PaymentStatementController {

    private final PaymentStatementService service;

    @PostMapping
    public ResponseEntity<PaymentStatementDTO> create(@RequestBody @Valid CreatePaymentStatementDTO dto) {
        var response = service.create(dto);

        return ResponseEntity
                .created(URI.create("/api/payment-statement/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentStatementDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentStatementDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentStatementDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePaymentStatementDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}