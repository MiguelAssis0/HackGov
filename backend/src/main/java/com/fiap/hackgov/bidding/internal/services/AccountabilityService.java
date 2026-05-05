package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.entities.Accountability;
import com.fiap.hackgov.bidding.internal.entities.enums.InstallmentStatus;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.repositories.AccountabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountabilityService {

    @Autowired
    private AccountabilityRepository accountabilityRepository;

    public Page<Accountability> getAllAccountabilities(Pageable pageable) {
        return accountabilityRepository.findAll(pageable);
    }

    public Optional<Accountability> getAccountabilityById(UUID id) {
        return accountabilityRepository.findById(id);
    }

    public Accountability createAccountability(Accountability accountability) {
        return accountabilityRepository.save(accountability);
    }

    public Optional<Accountability> updateAccountability(UUID id, Accountability updated) {
        return accountabilityRepository.findById(id)
                .map(existing -> {

                    if (updated.getProcessStage() != null)
                        existing.setProcessStage(updated.getProcessStage());

                    if (updated.getInstallmentStatus() != null)
                        existing.setInstallmentStatus(updated.getInstallmentStatus());

                    if (updated.getResponsibleId() != null)
                        existing.setResponsibleId(updated.getResponsibleId());

                    if (updated.getAnalysisDate() != null)
                        existing.setAnalysisDate(updated.getAnalysisDate());

                    return accountabilityRepository.save(existing);
                });
    }

    public boolean deleteAccountability(UUID id) {
        if (!accountabilityRepository.existsById(id)) return false;
        accountabilityRepository.deleteById(id);
        return true;
    }

    public Page<Accountability> getAccountabilitiesByProcessStage(ProcessStage processStage, Pageable pageable) {
        return accountabilityRepository.findByProcessStage(processStage, pageable);
    }

    public Page<Accountability> getAccountabilitiesByInstallmentStatus(InstallmentStatus installmentStatus, Pageable pageable) {
        return accountabilityRepository.findByInstallmentStatus(installmentStatus, pageable);
    }

    public Page<Accountability> getAccountabilitiesByResponsible(UUID responsibleId, Pageable pageable) {
        return accountabilityRepository.findByResponsibleId(responsibleId, pageable);
    }

    public Optional<Accountability> getAccountabilityByEffort(UUID effortId) {
        return accountabilityRepository.findByEffortId(effortId);
    }

    public Optional<Accountability> getAccountabilityByPaymentStatement(UUID paymentStatementId) {
        return accountabilityRepository.findByPaymentStatementId(paymentStatementId);
    }

    public Page<Accountability> getAccountabilitiesByAnalysisDateRange(Date startDate, Date endDate, Pageable pageable) {
        return accountabilityRepository.findByAnalysisDateBetween(startDate, endDate, pageable);
    }

    public Optional<Accountability> updateAccountabilityProcessStage(UUID id, ProcessStage processStage) {
        return accountabilityRepository.findById(id)
                .map(acc -> {
                    acc.setProcessStage(processStage);
                    return accountabilityRepository.save(acc);
                });
    }

    public Optional<Accountability> updateAccountabilityInstallmentStatus(UUID id, InstallmentStatus installmentStatus) {
        return accountabilityRepository.findById(id)
                .map(acc -> {
                    acc.setInstallmentStatus(installmentStatus);
                    return accountabilityRepository.save(acc);
                });
    }

    public Optional<Accountability> updateAccountabilityResponsible(UUID id, UUID responsibleId) {
        return accountabilityRepository.findById(id)
                .map(acc -> {
                    acc.setResponsibleId(responsibleId);
                    return accountabilityRepository.save(acc);
                });
    }

    public boolean existsByEffortId(UUID effortId) {
        return accountabilityRepository.existsByEffortId(effortId);
    }

    public boolean existsByPaymentStatementId(UUID paymentStatementId) {
        return accountabilityRepository.existsByPaymentStatementId(paymentStatementId);
    }

    public Page<Accountability> getAccountabilitiesByAnalysisDateBefore(Date date, Pageable pageable) {
        return accountabilityRepository.findByAnalysisDateBefore(date, pageable);
    }

    public Page<Accountability> getAccountabilitiesByAnalysisDateAfter(Date date, Pageable pageable) {
        return accountabilityRepository.findByAnalysisDateAfter(date, pageable);
    }
}