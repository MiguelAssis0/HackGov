package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.PaymentStatement.*;
import com.fiap.hackgov.bidding.internal.entities.PaymentStatement;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentStatementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "effort", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentStatement toEntity(CreatePaymentStatementDTO dto);

    PaymentStatementDTO toDTO(PaymentStatement entity);
}