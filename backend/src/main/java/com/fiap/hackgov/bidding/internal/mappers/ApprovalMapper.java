package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Approval.ApprovalResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.Approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.entities.Approval;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApprovalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Approval toEntity(CreateApprovalDTO dto);

    ApprovalResponseDTO toDTO(Approval approval);

    CreateApprovalDTO toCreateApprovalDTO(Approval approval);
}
