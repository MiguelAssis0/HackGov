package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.DTOs.notice.CreateNoticeDTO;
import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.entities.Notice;
import com.fiap.hackgov.bidding.internal.entities.enums.LicitationEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.NoticeStatus;
import com.fiap.hackgov.bidding.internal.mappers.NoticeMapper;
import com.fiap.hackgov.bidding.internal.repositories.LicitationProcessRepository;
import com.fiap.hackgov.bidding.internal.repositories.NoticeRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final LicitationProcessRepository licitationProcessRepository;
    private final LicitationProcessService licitationProcessService;
    private final NoticeMapper noticeMapper;

    public Notice create(CreateNoticeDTO dto, Employee employee) {

        LicitationProcess licitationProcess = licitationProcessRepository.findById(dto.licitationProcessId())
                .orElseThrow(() -> new ResourceNotFoundException("Licitation process not found: " + dto.licitationProcessId()));

        noticeRepository.findByLicitationProcessId(licitationProcess.getId()).ifPresent(existing -> {
            throw new BusinessException("Licitation process already has a notice");
        });

        if (noticeRepository.existsByNoticeNumber(dto.noticeNumber())) {
            throw new BusinessException("Notice number already exists");
        }

        validateDates(dto);

        Notice notice = noticeMapper.toEntity(dto);
        notice.setLicitationProcess(licitationProcess);
        notice.setCreatedBy(employee);

        notice = noticeRepository.save(notice);

        licitationProcessService.createHistory(
                licitationProcess,
                employee,
                dto.status() == NoticeStatus.PUBLISHED ? LicitationEventType.NOTICE_PUBLISHED : LicitationEventType.NOTICE_CREATED,
                licitationProcess.getStatus(),
                "Edital criado: " + dto.noticeNumber()
        );

        return notice;
    }

    @Transactional(readOnly = true)
    public Page<Notice> findAll(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Notice findById(UUID id) {
        return noticeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notice not found: " + id));
    }

    @Transactional(readOnly = true)
    public Notice findByLicitationProcessId(UUID licitationProcessId) {
        return noticeRepository.findByLicitationProcessId(licitationProcessId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found for licitation process: " + licitationProcessId));
    }

    private void validateDates(CreateNoticeDTO dto) {

        if (dto.proposalOpeningDate().isBefore(dto.publicationDate())) {
            throw new BusinessException("Proposal opening date cannot be before publication date");
        }

        if (dto.proposalClosingDate().isBefore(dto.proposalOpeningDate())) {
            throw new BusinessException("Proposal closing date cannot be before proposal opening date");
        }
    }
}
