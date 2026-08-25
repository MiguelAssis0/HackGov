package com.fiap.hackgov.inbox.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.inbox.internal.DTOs.InboxDTOs.Response;
import com.fiap.hackgov.inbox.internal.entities.InboxEntry;
import com.fiap.hackgov.inbox.internal.repositories.InboxEntryRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.entities.CrossSectorTaskRequest;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final InboxEntryRepository repository;

    @Transactional(readOnly = true)
    public Page<Response> findVisible(InboxEntry.Status status, InboxEntry.Type type, boolean unreadOnly,
                                      String query, Pageable pageable, Employee employee) {
        Employee current = requireEmployee(employee);
        UUID sectorId = current.getSectorId() == null ? null : current.getSectorId().getId();
        return repository.findVisible(cityHallId(current), current.getId(), sectorId,
                        Roles.ADMIN.equals(current.getRole()), status, type, unreadOnly,
                        query == null ? "" : query.trim(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public Response read(UUID id, Employee employee) {
        InboxEntry entry = visibleEntry(id, requireEmployee(employee));
        if (entry.getReadAt() == null) entry.setReadAt(LocalDateTime.now());
        return toResponse(repository.save(entry));
    }

    @Transactional
    public Response claim(UUID id, Employee employee) {
        Employee current = requireEmployee(employee);
        InboxEntry entry = visibleEntry(id, current);
        if (entry.getDestinationEmployee() != null && !entry.getDestinationEmployee().getId().equals(current.getId())) {
            throw new BusinessException("Uma entrada pessoal nao pode ser assumida por outro funcionario");
        }
        if (entry.getAssignedTo() != null && !entry.getAssignedTo().getId().equals(current.getId())) {
            throw new BusinessException("A entrada ja foi assumida por outro funcionario");
        }
        if (entry.getStatus() == InboxEntry.Status.COMPLETED || entry.getStatus() == InboxEntry.Status.ARCHIVED) {
            throw new BusinessException("Esta entrada nao pode mais ser assumida");
        }
        entry.setAssignedTo(current);
        entry.setStatus(InboxEntry.Status.IN_PROGRESS);
        if (entry.getReadAt() == null) entry.setReadAt(LocalDateTime.now());
        return toResponse(repository.save(entry));
    }

    @Transactional
    public Response complete(UUID id, Employee employee) {
        Employee current = requireEmployee(employee);
        InboxEntry entry = visibleEntry(id, current);
        boolean admin = Roles.ADMIN.equals(current.getRole());
        if (!admin && entry.getAssignedTo() != null && !entry.getAssignedTo().getId().equals(current.getId())) {
            throw new BusinessException("Somente o responsavel pode concluir esta entrada");
        }
        entry.setStatus(InboxEntry.Status.COMPLETED);
        if (entry.getReadAt() == null) entry.setReadAt(LocalDateTime.now());
        return toResponse(repository.save(entry));
    }

    @Transactional
    public InboxEntry notifyTask(Task task, Employee actor) {
        if (task.getResponsible() == null || task.getBoard() == null || task.getBoard().getCityHall() == null) return null;
        UUID cityId = task.getBoard().getCityHall().getId();
        String key = "task:" + task.getId() + ":employee:" + task.getResponsible().getId();
        InboxEntry entry = repository.findByCityHall_IdAndKey(cityId, key).orElseGet(InboxEntry::new);
        entry.setCityHall(task.getBoard().getCityHall());
        entry.setTitle("Tarefa atribuida: " + task.getTitle());
        entry.setDescription(task.getDescription() == null ? "" : task.getDescription());
        entry.setType(InboxEntry.Type.TASK);
        entry.setPriority(InboxEntry.Priority.NORMAL);
        entry.setDestinationEmployee(task.getResponsible());
        entry.setDestinationSector(task.getBoard().getSector());
        entry.setToolSlug("tarefas");
        entry.setObjectType("task");
        entry.setObjectId(task.getId());
        entry.setUrl("/tarefas?task=" + task.getId());
        entry.setKey(key);
        entry.setCreatedBy(actor);
        if (entry.getId() == null) entry.setStatus(InboxEntry.Status.NEW);
        return repository.save(entry);
    }

    @Transactional
    public InboxEntry notifyBiddingStage(Requisition requisition, ProcessStage stage, Employee actor) {
        if (requisition == null || requisition.getSector() == null || requisition.getSector().getCityHall() == null) return null;
        UUID cityId = requisition.getSector().getCityHall().getId();
        String key = "bidding:" + requisition.getId() + ":" + stage.name();
        InboxEntry entry = repository.findByCityHall_IdAndKey(cityId, key).orElseGet(InboxEntry::new);
        entry.setCityHall(requisition.getSector().getCityHall());
        entry.setTitle("Processo " + requisition.getRegisterNumber() + ": " + stage.getDescription());
        entry.setDescription(requisition.getTechnicalDescription() == null ? "" : requisition.getTechnicalDescription());
        entry.setType(InboxEntry.Type.REQUEST);
        entry.setPriority(InboxEntry.Priority.NORMAL);
        entry.setDestinationSector(requisition.getSector());
        entry.setDestinationEmployee(resolveStageDestination(requisition, stage));
        entry.setToolSlug("compras-licitacoes");
        entry.setObjectType("requisition");
        entry.setObjectId(requisition.getId());
        entry.setUrl("/processos?requisition=" + requisition.getId());
        entry.setKey(key);
        entry.setCreatedBy(actor);
        if (entry.getId() == null) entry.setStatus(InboxEntry.Status.NEW);
        return repository.save(entry);
    }

    private Employee resolveStageDestination(Requisition requisition, ProcessStage stage) {
        return stage.getStep() >= ProcessStage.ANALISE_REQUISICAO.getStep()
                && requisition.getProcurementResponsible() != null
                ? requisition.getProcurementResponsible()
                : requisition.getResponsible();
    }

    @Transactional
    public InboxEntry notifyCrossSectorRequest(CrossSectorTaskRequest request) {
        InboxEntry entry = new InboxEntry(); entry.setCityHall(request.getCityHall());
        entry.setTitle("Demanda de " + request.getOriginSector().getName() + ": " + request.getTitle());
        entry.setDescription(request.getDescription()); entry.setType(InboxEntry.Type.REQUEST);
        entry.setPriority(request.getPriority() == Task.Priority.URGENT || request.getPriority() == Task.Priority.HIGH ? InboxEntry.Priority.HIGH : InboxEntry.Priority.NORMAL);
        entry.setDestinationSector(request.getDestinationSector()); entry.setToolSlug("tarefas");
        entry.setObjectType("cross_sector_task_request"); entry.setObjectId(request.getId());
        entry.setUrl("/caixa-entrada"); entry.setKey("task-request:" + request.getId() + ":destination"); entry.setCreatedBy(request.getRequestedBy());
        return repository.save(entry);
    }

    @Transactional
    public void completeObject(UUID cityId, String objectType, UUID objectId) {
        repository.findByCityHall_IdAndKey(cityId, "task-request:" + objectId + ":destination").ifPresent(entry -> {
            entry.setStatus(InboxEntry.Status.COMPLETED); if (entry.getReadAt() == null) entry.setReadAt(LocalDateTime.now()); repository.save(entry);
        });
    }

    @Transactional
    public InboxEntry notifyRequestResult(CrossSectorTaskRequest request, String result) {
        InboxEntry entry = new InboxEntry(); entry.setCityHall(request.getCityHall()); entry.setTitle("Demanda " + result + ": " + request.getTitle());
        entry.setDescription(request.getFeedback()); entry.setType(InboxEntry.Type.REQUEST); entry.setDestinationSector(request.getOriginSector());
        entry.setDestinationEmployee(request.getRequestedBy()); entry.setToolSlug("tarefas"); entry.setObjectType("cross_sector_task_request");
        entry.setObjectId(request.getId()); entry.setUrl(request.getGeneratedTask()==null?"/tarefas":"/tarefas?task="+request.getGeneratedTask().getId());
        entry.setKey("task-request:" + request.getId() + ":result"); entry.setCreatedBy(request.getAnsweredBy()); return repository.save(entry);
    }

    private InboxEntry visibleEntry(UUID id, Employee employee) {
        InboxEntry entry = repository.findByIdAndCityHall_Id(id, cityHallId(employee))
                .orElseThrow(() -> new ResourceNotFoundException("Entrada nao encontrada"));
        if (Roles.ADMIN.equals(employee.getRole())) return entry;
        boolean personal = entry.getDestinationEmployee() != null && entry.getDestinationEmployee().getId().equals(employee.getId());
        boolean sector = entry.getDestinationEmployee() == null && employee.getSectorId() != null
                && entry.getDestinationSector() != null && entry.getDestinationSector().getId().equals(employee.getSectorId().getId());
        if (!personal && !sector) throw new ResourceNotFoundException("Entrada nao encontrada");
        return entry;
    }

    private Employee requireEmployee(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado para acessar a caixa de entrada");
        cityHallId(employee);
        return employee;
    }

    private UUID cityHallId(Employee employee) {
        if (employee.getCityHallId() == null) throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee.getCityHallId().getId();
    }

    private Response toResponse(InboxEntry entry) {
        return new Response(entry.getId(), entry.getTitle(), entry.getDescription(), entry.getType(), entry.getStatus(), entry.getPriority(),
                entry.getDestinationSector() == null ? null : entry.getDestinationSector().getId(),
                entry.getDestinationSector() == null ? null : entry.getDestinationSector().getName(),
                entry.getDestinationEmployee() == null ? null : entry.getDestinationEmployee().getId(), employeeName(entry.getDestinationEmployee()),
                entry.getAssignedTo() == null ? null : entry.getAssignedTo().getId(), employeeName(entry.getAssignedTo()),
                entry.getToolSlug(), entry.getObjectType(), entry.getObjectId(), entry.getUrl(), employeeName(entry.getCreatedBy()),
                entry.getReadAt(), entry.getCreatedAt(), entry.getUpdatedAt());
    }

    private String employeeName(Employee employee) {
        return employee == null ? null : employee.getFullName();
    }
}
