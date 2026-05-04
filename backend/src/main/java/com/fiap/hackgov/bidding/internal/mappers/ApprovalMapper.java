package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.Approval.CreateApprovalDTO;
import com.fiap.hackgov.bidding.internal.entities.Approval;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApprovalMapper {

    Approval toEntity(CreateApprovalDTO createApprovalDTO);

}
