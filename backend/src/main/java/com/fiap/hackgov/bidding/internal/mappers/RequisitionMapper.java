package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.CreateRequisitionDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Requisiton.RequisitionResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = ApprovalMapper.class
)
public interface RequisitionMapper {

    Requisition toEntity(CreateRequisitionDTO createRequisitionDTO);

    List<RequisitionResponseDTO> toDTOList(List<Requisition> requisitions);

    RequisitionResponseDTO toDTO(Requisition requisition);

}
