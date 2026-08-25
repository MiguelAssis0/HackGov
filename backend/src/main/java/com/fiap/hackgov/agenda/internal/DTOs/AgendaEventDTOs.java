package com.fiap.hackgov.agenda.internal.DTOs;

import com.fiap.hackgov.agenda.internal.entities.AgendaEvent;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public final class AgendaEventDTOs {
    private AgendaEventDTOs() {}

    public record SaveRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 5000) String description,
            @NotNull AgendaEvent.Type type,
            @NotNull LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
            @Size(max = 180) String location,
            UUID taskId
    ) {
        @AssertTrue(message = "A data final nao pode ser anterior a data inicial")
        public boolean isDateRangeValid() {
            return startDate == null || endDate == null || !endDate.isBefore(startDate);
        }

        @AssertTrue(message = "O horario final precisa ser posterior ao horario inicial")
        public boolean isTimeRangeValid() {
            return startDate == null || (endDate != null && !startDate.equals(endDate))
                    || startTime == null || endTime == null || endTime.isAfter(startTime);
        }
    }

    public record Response(
            UUID id,
            String title,
            String description,
            AgendaEvent.Type type,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
            String location,
            UUID taskId,
            String taskTitle,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
