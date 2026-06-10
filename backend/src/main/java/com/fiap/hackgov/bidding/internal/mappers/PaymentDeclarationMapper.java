package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.paymentDeclaration.CreatePaymentDeclarationDTO;
import com.fiap.hackgov.bidding.internal.DTOs.paymentDeclaration.PaymentDeclarationResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.PaymentDeclaration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentDeclarationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commitment", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentDeclaration toEntity(CreatePaymentDeclarationDTO dto);

    @Mapping(target = "commitmentId", source = "commitment.id")
    @Mapping(target = "commitmentNumber", source = "commitment.commitmentNumber")
    @Mapping(target = "contractId", source = "commitment.contract.id")
    @Mapping(target = "contractNumber", source = "commitment.contract.contractNumber")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", expression = "java(entity.getApprovedBy().getFullName())")
    PaymentDeclarationResponseDTO toDTO(PaymentDeclaration entity);

    List<PaymentDeclarationResponseDTO> toDTOList(List<PaymentDeclaration> entities);
}
