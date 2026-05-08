package com.fiap.hackgov.shared.infra.pagination;

import java.util.List;

public record PageResponseDTO<T>(

        List<T> items,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean last

) {
}