package com.fiap.hackgov.bidding.internal.DTOs;

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
