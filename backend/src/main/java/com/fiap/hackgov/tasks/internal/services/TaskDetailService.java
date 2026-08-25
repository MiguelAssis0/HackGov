package com.fiap.hackgov.tasks.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.DTOs.TaskDetailDTOs.*;
import com.fiap.hackgov.tasks.internal.entities.*;
import com.fiap.hackgov.tasks.internal.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskDetailService {
    private static final long MAX_ATTACHMENT_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "csv", "txt", "png", "jpg", "jpeg");

    private final TaskReporitory taskRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskChecklistRepository checklistRepository;
    private final TaskTimeEntryRepository timeRepository;
    private final TaskAttachmentRepository attachmentRepository;

    @Transactional(readOnly = true)
    public DetailResponse detail(UUID taskId, Employee employee) {
        Task task = visibleTask(taskId, employee);
        return new DetailResponse(
                commentRepository.findByTask_IdOrderByCreatedAtAsc(task.getId()).stream().map(this::commentResponse).toList(),
                checklistRepository.findByTask_IdOrderByOrderIndexAscCreatedAtAsc(task.getId()).stream().map(this::checklistResponse).toList(),
                timeRepository.findByTask_IdOrderByCreatedAtDesc(task.getId()).stream().map(this::timeResponse).toList(),
                attachmentRepository.findByTask_IdOrderByCreatedAtDesc(task.getId()).stream().map(this::attachmentResponse).toList()
        );
    }

    @Transactional
    public CommentResponse addComment(UUID taskId, String text, Employee employee) {
        Task task = visibleTask(taskId, employee);
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(requireEmployee(employee));
        comment.setText(requireText(text, 2000));
        return commentResponse(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse updateComment(UUID taskId, UUID commentId, String text, Employee employee) {
        visibleTask(taskId, employee);
        TaskComment comment = commentRepository.findByIdAndTask_Id(commentId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario nao encontrado"));
        requireOwner(comment.getAuthor(), employee, "Somente o autor pode editar o comentario");
        comment.setText(requireText(text, 2000));
        return commentResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID taskId, UUID commentId, Employee employee) {
        visibleTask(taskId, employee);
        TaskComment comment = commentRepository.findByIdAndTask_Id(commentId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario nao encontrado"));
        requireOwner(comment.getAuthor(), employee, "Somente o autor pode excluir o comentario");
        commentRepository.delete(comment);
    }

    @Transactional
    public ChecklistResponse addChecklist(UUID taskId, String title, Employee employee) {
        Task task = managedTask(taskId, employee);
        TaskChecklistItem item = new TaskChecklistItem();
        item.setTask(task);
        item.setTitle(requireText(title, 180));
        item.setOrderIndex((int) checklistRepository.countByTask_Id(taskId));
        return checklistResponse(checklistRepository.save(item));
    }

    @Transactional
    public ChecklistResponse updateChecklist(UUID taskId, UUID itemId, String title, Employee employee) {
        managedTask(taskId, employee);
        TaskChecklistItem item = checklistRepository.findByIdAndTask_Id(itemId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de checklist nao encontrado"));
        item.setTitle(requireText(title, 180));
        return checklistResponse(checklistRepository.save(item));
    }

    @Transactional
    public ChecklistResponse toggleChecklist(UUID taskId, UUID itemId, Employee employee) {
        managedTask(taskId, employee);
        TaskChecklistItem item = checklistRepository.findByIdAndTask_Id(itemId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de checklist nao encontrado"));
        item.setCompleted(!item.isCompleted());
        item.setCompletedBy(item.isCompleted() ? employee : null);
        item.setCompletedAt(item.isCompleted() ? LocalDateTime.now() : null);
        return checklistResponse(checklistRepository.save(item));
    }

    @Transactional
    public List<ChecklistResponse> reorderChecklist(UUID taskId, List<UUID> ids, Employee employee) {
        managedTask(taskId, employee);
        List<TaskChecklistItem> current = checklistRepository.findByTask_IdOrderByOrderIndexAscCreatedAtAsc(taskId);
        if (ids == null || ids.size() != current.size() || new HashSet<>(ids).size() != ids.size()
                || !new HashSet<>(ids).equals(current.stream().map(TaskChecklistItem::getId).collect(java.util.stream.Collectors.toSet()))) {
            throw new BusinessException("A nova ordem precisa conter todos os itens uma unica vez");
        }
        Map<UUID, TaskChecklistItem> byId = new HashMap<>();
        current.forEach(item -> byId.put(item.getId(), item));
        for (int index = 0; index < ids.size(); index++) byId.get(ids.get(index)).setOrderIndex(index);
        return checklistRepository.saveAll(current).stream().sorted(Comparator.comparingInt(TaskChecklistItem::getOrderIndex))
                .map(this::checklistResponse).toList();
    }

    @Transactional
    public void deleteChecklist(UUID taskId, UUID itemId, Employee employee) {
        managedTask(taskId, employee);
        TaskChecklistItem item = checklistRepository.findByIdAndTask_Id(itemId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de checklist nao encontrado"));
        checklistRepository.delete(item);
    }

    @Transactional
    public TimeResponse startTimer(UUID taskId, Employee employee) {
        Task task = managedTask(taskId, employee);
        timeRepository.findFirstByEmployee_IdAndManualFalseAndFinishedAtIsNull(employee.getId()).ifPresent(active -> {
            throw new BusinessException("O funcionario ja possui um cronometro ativo");
        });
        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setTask(task);
        entry.setEmployee(employee);
        entry.setStartedAt(LocalDateTime.now());
        entry.setReferenceDate(LocalDate.now());
        return timeResponse(timeRepository.save(entry));
    }

    @Transactional
    public TimeResponse pauseTimer(UUID taskId, Employee employee) {
        managedTask(taskId, employee);
        TaskTimeEntry entry = timeRepository.findFirstByEmployee_IdAndManualFalseAndFinishedAtIsNull(employee.getId())
                .filter(active -> active.getTask().getId().equals(taskId))
                .orElseThrow(() -> new BusinessException("Nao existe cronometro ativo para esta tarefa"));
        LocalDateTime finished = LocalDateTime.now();
        entry.setFinishedAt(finished);
        entry.setDurationSeconds(Math.max(0, Duration.between(entry.getStartedAt(), finished).getSeconds()));
        return timeResponse(timeRepository.save(entry));
    }

    @Transactional
    public TimeResponse addManualTime(UUID taskId, ManualTimeRequest request, Employee employee) {
        Task task = managedTask(taskId, employee);
        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setTask(task);
        entry.setEmployee(employee);
        entry.setManual(true);
        entry.setReferenceDate(request.referenceDate());
        entry.setDurationSeconds(Math.round(request.hours() * 3600));
        entry.setObservation(requireText(request.observation(), 500));
        return timeResponse(timeRepository.save(entry));
    }

    @Transactional
    public void deleteTime(UUID taskId, UUID entryId, Employee employee) {
        visibleTask(taskId, employee);
        TaskTimeEntry entry = timeRepository.findByIdAndTask_Id(entryId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Apontamento nao encontrado"));
        requireOwner(entry.getEmployee(), employee, "Somente o funcionario pode excluir o proprio apontamento");
        timeRepository.delete(entry);
    }

    @Transactional
    public AttachmentResponse addAttachment(UUID taskId, MultipartFile file, Employee employee) {
        Task task = visibleTask(taskId, employee);
        byte[] content = validateFile(file);
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setOriginalName(safeFileName(file.getOriginalFilename()));
        attachment.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        attachment.setSize(content.length);
        attachment.setContent(content);
        attachment.setUploadedBy(employee);
        return attachmentResponse(attachmentRepository.save(attachment));
    }

    @Transactional(readOnly = true)
    public TaskAttachment getAttachment(UUID taskId, UUID attachmentId, Employee employee) {
        visibleTask(taskId, employee);
        return attachmentRepository.findByIdAndTask_Id(attachmentId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo nao encontrado"));
    }

    @Transactional
    public void deleteAttachment(UUID taskId, UUID attachmentId, Employee employee) {
        visibleTask(taskId, employee);
        TaskAttachment attachment = attachmentRepository.findByIdAndTask_Id(attachmentId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo nao encontrado"));
        requireOwner(attachment.getUploadedBy(), employee, "Somente quem enviou ou um administrador pode excluir o anexo");
        attachmentRepository.delete(attachment);
    }

    private Task visibleTask(UUID taskId, Employee employee) {
        Employee current = requireEmployee(employee);
        UUID cityId = current.getCityHallId().getId();
        if (Roles.ADMIN.equals(current.getRole())) {
            return taskRepository.findByIdAndBoard_CityHall_Id(taskId, cityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarefa nao encontrada"));
        }
        if (current.getSectorId() == null) throw new BusinessException("O funcionario precisa estar vinculado a um setor");
        return taskRepository.findByIdAndBoard_CityHall_IdAndBoard_Sector_Id(taskId, cityId, current.getSectorId().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa nao encontrada para o seu setor"));
    }

    private Task managedTask(UUID taskId, Employee employee) {
        Task task = visibleTask(taskId, employee);
        if (Roles.ADMIN.equals(employee.getRole())) return task;
        boolean responsible = task.getResponsible() != null && task.getResponsible().getId().equals(employee.getId())
                || task.getResponsibles().stream().anyMatch(item -> item.getId().equals(employee.getId()));
        if (!responsible) throw new UnauthorizedException("Somente responsaveis ou administradores podem gerenciar esta tarefa");
        return task;
    }

    private Employee requireEmployee(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        if (employee.getCityHallId() == null) throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee;
    }

    private void requireOwner(Employee owner, Employee employee, String message) {
        if (!Roles.ADMIN.equals(employee.getRole()) && (owner == null || !owner.getId().equals(employee.getId()))) {
            throw new UnauthorizedException(message);
        }
    }

    private String requireText(String value, int max) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) throw new BusinessException("O texto nao pode ficar vazio");
        if (text.length() > max) throw new BusinessException("O texto excede o limite de " + max + " caracteres");
        return text;
    }

    private byte[] validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("Selecione um arquivo");
        if (file.getSize() > MAX_ATTACHMENT_SIZE) throw new BusinessException("O anexo nao pode exceder 10 MB");
        String name = safeFileName(file.getOriginalFilename());
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) throw new BusinessException("Tipo de arquivo nao permitido");
        try {
            byte[] content = file.getBytes();
            if (content.length == 0 || hasBlockedSignature(content)) throw new BusinessException("Conteudo de arquivo invalido");
            return content;
        } catch (IOException exception) {
            throw new BusinessException("Nao foi possivel ler o arquivo");
        }
    }

    private boolean hasBlockedSignature(byte[] content) {
        return content.length >= 2 && ((content[0] == 'M' && content[1] == 'Z') || (content[0] == '#' && content[1] == '!'));
    }

    private String safeFileName(String name) {
        String normalized = name == null ? "arquivo" : name.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1).replaceAll("[\\r\\n]", "").trim();
        return normalized.isEmpty() ? "arquivo" : normalized.substring(0, Math.min(255, normalized.length()));
    }

    private CommentResponse commentResponse(TaskComment item) { return new CommentResponse(item.getId(), item.getText(), id(item.getAuthor()), name(item.getAuthor()), item.getCreatedAt(), item.getEditedAt()); }
    private ChecklistResponse checklistResponse(TaskChecklistItem item) { return new ChecklistResponse(item.getId(), item.getTitle(), item.getOrderIndex(), item.isCompleted(), id(item.getCompletedBy()), name(item.getCompletedBy()), item.getCompletedAt()); }
    private TimeResponse timeResponse(TaskTimeEntry item) {
        boolean active = !item.isManual() && item.getFinishedAt() == null;
        long seconds = active && item.getStartedAt() != null ? Math.max(0, Duration.between(item.getStartedAt(), LocalDateTime.now()).getSeconds()) : item.getDurationSeconds();
        return new TimeResponse(item.getId(), id(item.getEmployee()), name(item.getEmployee()), item.getStartedAt(), item.getFinishedAt(), seconds, active, item.isManual(), item.getReferenceDate(), item.getObservation());
    }
    private AttachmentResponse attachmentResponse(TaskAttachment item) { return new AttachmentResponse(item.getId(), item.getOriginalName(), item.getContentType(), item.getSize(), id(item.getUploadedBy()), name(item.getUploadedBy()), item.getCreatedAt()); }
    private UUID id(Employee employee) { return employee == null ? null : employee.getId(); }
    private String name(Employee employee) { return employee == null ? null : employee.getFullName(); }
}
