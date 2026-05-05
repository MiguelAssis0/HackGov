package com.fiap.hackgov.bidding.internal.DTOs.Supplier;

import com.fiap.hackgov.bidding.internal.DTOs.Bidding.BiddingProcessDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Contract.ContractDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SupplierDTO(
        UUID id,
        String socialReason,
        String cnpj,
        String email,
        String phone,
        String bank,
        String bankAgency,
        String bankAccount,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<BiddingProcessDTO> biddingProcesses,
        List<ContractDTO> contracts
) {
}
