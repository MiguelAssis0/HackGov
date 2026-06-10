package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport.AccountabilityReportResponseDTO;
import com.fiap.hackgov.bidding.internal.DTOs.accountabilityReport.CreateAccountabilityReportDTO;
import com.fiap.hackgov.bidding.internal.entities.AccountabilityReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountabilityReportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccountabilityReport toEntity(CreateAccountabilityReportDTO dto);

    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "contractNumber", source = "contract.contractNumber")
    @Mapping(target = "responsibleId", source = "responsible.id")
    @Mapping(target = "responsibleName", expression = "java(entity.getResponsible().getFullName())")
    AccountabilityReportResponseDTO toDTO(AccountabilityReport entity);

    List<AccountabilityReportResponseDTO> toDTOList(List<AccountabilityReport> entities);
}
