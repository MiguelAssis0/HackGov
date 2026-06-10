package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.licitation.CreateLicitationProcessDTO;
import com.fiap.hackgov.bidding.internal.DTOs.licitation.LicitationProcessResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LicitationProcessMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processNumber", ignore = true)
    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "winnerSupplier", ignore = true)
    @Mapping(target = "notice", ignore = true)
    @Mapping(target = "contract", ignore = true)
    @Mapping(target = "proposals", ignore = true)
    @Mapping(target = "histories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LicitationProcess toEntity(CreateLicitationProcessDTO dto);

    @Mapping(target = "requisitionId", source = "requisition.id")
    @Mapping(target = "requisitionNumber", source = "requisition.registerNumber")
    @Mapping(target = "responsibleId", source = "responsible.id")
    @Mapping(target = "responsibleName", source = "responsible")
    @Mapping(target = "winnerSupplierId", source = "winnerSupplier.id")
    @Mapping(target = "winnerSupplierName", source = "winnerSupplier.corporateName")
    @Mapping(target = "winnerSupplierCnpj", source = "winnerSupplier.cnpj")
    LicitationProcessResponseDTO toDTO(LicitationProcess entity);

    List<LicitationProcessResponseDTO> toDTOList(List<LicitationProcess> entities);

    default String map(Employee employee) {
        return employee == null ? null : employee.getFullName();
    }
}
