package com.fiap.hackgov.bidding.internal.DTOs;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record PageResponseDTO<T>(
        List<T> content,
        PageMetadata page
) {
    public record PageMetadata(
            int number,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean hasNext,
            boolean hasPrevious
    ) {
        public static PageMetadata of(Page<?> page) {
            return new PageMetadata(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast(),
                    page.hasNext(),
                    page.hasPrevious()
            );
        }
    }

    public static <T> PageResponseDTO<T> of(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                PageMetadata.of(page)
        );
    }

    public static <T, R> PageResponseDTO<R> of(Page<T> page, Function<T, R> mapper) {
        List<R> mappedContent = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                mappedContent,
                PageMetadata.of(page)
        );
    }
}
