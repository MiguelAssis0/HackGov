package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Edital.*;
import com.fiap.hackgov.bidding.internal.entities.Edital;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EditalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "biddingProcess", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Edital toEntity(CreateEditalDTO dto);

    @Mapping(source = "biddingProcess.id", target = "biddingProcessId")
    EditalDTO toDTO(Edital entity);
}