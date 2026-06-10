package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.approval.ApprovalResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.entities.Approval;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApprovalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "approvalSector", source = "stage")
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Approval toEntity(CreateApprovalDTO dto);

    @Mapping(
            target = "requisitionId",
            source = "requisition.id"
    )
    @Mapping(
            target = "approvedById",
            source = "approvedBy.id"
    )
    @Mapping(
            target = "sector",
            source = "approvalSector"
    )
    @Mapping(
            target = "status",
            source = "approvalStatus"
    )
    ApprovalResponseDTO toDTO(Approval approval);

    @Mapping(target = "stage", source = "approvalSector")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    CreateApprovalDTO toCreateApprovalDTO(Approval approval);
}
