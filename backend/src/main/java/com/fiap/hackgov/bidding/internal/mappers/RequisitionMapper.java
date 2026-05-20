package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.requisiton.*;
import com.fiap.hackgov.bidding.internal.entities.ProcessHistory;
import com.fiap.hackgov.bidding.internal.entities.ProcessStatus;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = ETPMapper.class)
public interface RequisitionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registerNumber", ignore = true)
    @Mapping(target = "processStatus", ignore = true)
    @Mapping(target = "approvals", ignore = true)
    @Mapping(target = "histories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "requiresEtp", ignore = true)
    @Mapping(target = "requestStatus", ignore = true)
    @Mapping(target = "etp", ignore = true)
    @Mapping(target = "sector", ignore = true)
    @Mapping(target = "responsible", ignore = true)
    Requisition toEntity(CreateRequisitionDTO dto);

    @Mapping(source = "processStatus.stage", target = "currentStage", qualifiedByName = "mapCurrentStage")
    @Mapping(source = "sector", target = "sector", qualifiedByName = "mapSector")
    @Mapping(source = "responsible", target = "responsible", qualifiedByName = "mapResponsible")
    @Mapping(source = "etp", target = "etp")
    RequisitionResponseDTO toDTO(Requisition entity);

    @Named("mapCurrentStage")
    default RequisitionCurrentStageDTO mapCurrentStage(ProcessStage stage) {

        if (stage == null) {
            return null;
        }

        return new RequisitionCurrentStageDTO(stage.name(), stage.getStep(), stage.getDescription());
    }

    @Named("mapSector")
    default RequisitionSectorDTO mapSector(Sector sector) {

        if (sector == null) {
            return null;
        }

        return new RequisitionSectorDTO(sector.getId(), sector.getName());
    }

    @Named("mapResponsible")
    default RequisitionResponsibleDTO mapResponsible(Employee employee) {

        if (employee == null) {
            return null;
        }

        return new RequisitionResponsibleDTO(employee.getId(), employee.getFirstName() + " " + employee.getLastName());
    }

    default ProcessStatus toInitialProcessStatus(Requisition requisition, UUID responsibleId) {

        ProcessStatus processStatus = new ProcessStatus();

        processStatus.setStage(ProcessStage.REQUISICAO_CADASTRADA);

        processStatus.setStartedAt(LocalDateTime.now());

        processStatus.setResponsibleId(responsibleId);

        processStatus.setRequisition(requisition);

        return processStatus;
    }

    default ProcessHistory toInitialHistory(Requisition requisition, Employee employee) {

        ProcessHistory history = new ProcessHistory();

        history.setStage(ProcessStage.REQUISICAO_CADASTRADA);

        history.setChangedBy(employee);

        history.setChangedAt(LocalDateTime.now());

        history.setObservation("Requisição criada");

        history.setRequisition(requisition);

        return history;
    }
}
