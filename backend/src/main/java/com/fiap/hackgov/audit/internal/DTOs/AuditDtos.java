package com.fiap.hackgov.audit.internal.DTOs;

import java.util.List;

public final class AuditDtos {
    private AuditDtos() {
    }

    public record Row(
            Long id,
            String dataHora,
            String usuario,
            String usuarioMascarado,
            String prefeitura,
            String modulo,
            String acao,
            String resultado,
            String objeto,
            String descricao,
            String ip,
            String tipo,
            String requestId,
            String hash
    ) {
    }

    public record ActionOption(String value, String label) {
    }

    public record CityHallOption(String id, String name) {
    }

    public record Page(
            List<Row> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            String scope,
            boolean canExport,
            boolean canViewSensitive,
            List<ActionOption> actionOptions,
            List<CityHallOption> cityHallOptions
    ) {
    }
}
