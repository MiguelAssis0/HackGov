package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.entities.Accountability;
import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.services.AccountabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/accountabilities")
@RequiredArgsConstructor
public class AccountabilityController {

    private AccountabilityService service;

    @GetMapping
    public ResponseEntity<Page<Accountability>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAllAccountabilities(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Accountability> getById(@PathVariable UUID id) {
        return service.getAccountabilityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Accountability> create(@RequestBody Accountability accountability) {
        return ResponseEntity.ok(service.createAccountability(accountability));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Accountability> update(@PathVariable UUID id,
                                                 @RequestBody Accountability accountability) {
        return service.updateAccountability(id, accountability)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (service.deleteAccountability(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/process-stage")
    public ResponseEntity<Page<Accountability>> byProcessStage(
            @RequestParam ProcessStage processStage,
            Pageable pageable) {
        return ResponseEntity.ok(service.getAccountabilitiesByProcessStage(processStage, pageable));
    }

    @GetMapping("/installment-status")
    public ResponseEntity<Page<Accountability>> byInstallmentStatus(
            @RequestParam InstallmentStatus installmentStatus,
            Pageable pageable) {
        return ResponseEntity.ok(service.getAccountabilitiesByInstallmentStatus(installmentStatus, pageable));
    }

    @GetMapping("/responsible/{id}")
    public ResponseEntity<Page<Accountability>> byResponsible(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(service.getAccountabilitiesByResponsible(id, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<Accountability>> byDateRange(
            @RequestParam Date start,
            @RequestParam Date end,
            Pageable pageable) {
        return ResponseEntity.ok(service.getAccountabilitiesByAnalysisDateRange(start, end, pageable));
    }
}