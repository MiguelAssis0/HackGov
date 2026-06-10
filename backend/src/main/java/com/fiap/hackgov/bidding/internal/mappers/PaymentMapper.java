package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.payment.CreatePaymentDTO;
import com.fiap.hackgov.bidding.internal.DTOs.payment.PaymentResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "declaration", ignore = true)
    @Mapping(target = "treasuryResponsible", ignore = true)
    @Mapping(target = "treasurySector", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toEntity(CreatePaymentDTO dto);

    @Mapping(target = "declarationId", source = "declaration.id")
    @Mapping(target = "commitmentId", source = "declaration.commitment.id")
    @Mapping(target = "contractId", source = "declaration.commitment.contract.id")
    @Mapping(target = "contractNumber", source = "declaration.commitment.contract.contractNumber")
    @Mapping(target = "treasuryResponsibleId", source = "treasuryResponsible.id")
    @Mapping(target = "treasuryResponsibleName", expression = "java(entity.getTreasuryResponsible().getFullName())")
    @Mapping(target = "treasurySectorId", source = "treasurySector.id")
    @Mapping(target = "treasurySectorName", source = "treasurySector.name")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", expression = "java(entity.getApprovedBy().getFullName())")
    PaymentResponseDTO toDTO(Payment entity);

    List<PaymentResponseDTO> toDTOList(List<Payment> entities);
}
