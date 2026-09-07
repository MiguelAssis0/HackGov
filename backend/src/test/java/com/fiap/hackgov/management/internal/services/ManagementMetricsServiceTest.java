package com.fiap.hackgov.management.internal.services;

import com.fiap.hackgov.management.internal.DTOs.ManagementResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManagementMetricsServiceTest {
    @Test
    void projectsRecentDeliveryTrend() {
        ManagementResponse metrics = new ManagementResponse(
                "Prefeitura", true, List.of(),
                new ManagementResponse.Period("mes", "Mês", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                null, false, List.of(),
                new ManagementResponse.Indicators(12, 120, 4, 30, 10, 3),
                List.of(), List.of(),
                List.of(
                        new ManagementResponse.TemporalPoint("01/01", 0),
                        new ManagementResponse.TemporalPoint("02/01", 0),
                        new ManagementResponse.TemporalPoint("03/01", 0),
                        new ManagementResponse.TemporalPoint("04/01", 2),
                        new ManagementResponse.TemporalPoint("05/01", 4),
                        new ManagementResponse.TemporalPoint("06/01", 6)),
                List.of(), 0);

        ManagementResponse.Forecast forecast = ManagementMetricsService.forecast(metrics);

        assertThat(forecast.trend()).isEqualTo("alta");
        assertThat(forecast.projectedTasks()).isPositive();
        assertThat(forecast.nextPeriods()).hasSize(3);
    }
}
