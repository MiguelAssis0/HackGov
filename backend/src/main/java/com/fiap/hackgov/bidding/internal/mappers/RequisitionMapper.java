package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.RequisitionDTO;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequisitionMapper {

    Requisition toEntity(CreateRequisitionDTO createRequisitionDTO);

    List<RequisitionDTO> toDTOList(List<Requisition> requisitions);

    RequisitionDTO toDTO(Requisition requisition);

}
