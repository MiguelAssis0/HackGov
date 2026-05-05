package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Payment.*;
import com.fiap.hackgov.bidding.internal.entities.Payment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "approved", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paymentStatement", ignore = true)
    Payment toEntity(CreatePaymentDTO dto);

    PaymentDTO toDTO(Payment payment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdatePaymentDTO dto, @MappingTarget Payment entity);
}