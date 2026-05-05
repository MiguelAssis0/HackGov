package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Bidding.*;
import com.fiap.hackgov.bidding.internal.entities.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BiddingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "edital", ignore = true)
    @Mapping(target = "winningSupplier", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BiddingProcess toEntity(CreateBiddingProcessDTO dto);

    BiddingProcessDTO toDTO(BiddingProcess entity);
}