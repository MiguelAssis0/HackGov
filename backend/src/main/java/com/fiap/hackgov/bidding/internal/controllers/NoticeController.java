package com.fiap.hackgov.bidding.internal.controllers;

import com.fiap.hackgov.bidding.internal.DTOs.notice.CreateNoticeDTO;
import com.fiap.hackgov.bidding.internal.DTOs.notice.NoticeResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Notice;
import com.fiap.hackgov.bidding.internal.mappers.NoticeMapper;
import com.fiap.hackgov.bidding.internal.services.NoticeService;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.pagination.PageResponseDTO;
import com.fiap.hackgov.shared.infra.pagination.PaginationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeMapper noticeMapper;
    private final PaginationMapper paginationMapper;

    @PostMapping
    public ResponseEntity<NoticeResponseDTO> create(@Valid @RequestBody CreateNoticeDTO dto, @AuthenticationPrincipal Employee employee) {

        Notice notice = noticeService.create(dto, employee);

        return ResponseEntity.status(HttpStatus.CREATED).body(noticeMapper.toDTO(notice));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<NoticeResponseDTO>> findAll(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NoticeResponseDTO> dtoPage = noticeService.findAll(pageable).map(noticeMapper::toDTO);

        return ResponseEntity.ok(paginationMapper.toDTO(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(noticeMapper.toDTO(noticeService.findById(id)));
    }

    @GetMapping("/licitation-process/{licitationProcessId}")
    public ResponseEntity<NoticeResponseDTO> findByLicitationProcessId(@PathVariable UUID licitationProcessId) {
        return ResponseEntity.ok(noticeMapper.toDTO(noticeService.findByLicitationProcessId(licitationProcessId)));
    }
}
