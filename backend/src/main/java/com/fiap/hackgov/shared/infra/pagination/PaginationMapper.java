package com.fiap.hackgov.shared.infra.pagination;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaginationMapper {

    public <T> PageResponseDTO<T> toDTO(
            Page<T> page
    ) {

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}