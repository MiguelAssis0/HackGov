package com.fiap.hackgov.agriculture.internal.DTOs;
import com.fiap.hackgov.agriculture.internal.entities.*;import jakarta.validation.constraints.*;import java.math.BigDecimal;import java.time.*;import java.util.*;
public final class AgricultureDTOs {private AgricultureDTOs(){}
 public record CatalogRequest(@NotBlank @Size(max=140) String name,AgriculturalServiceType.Area area,@DecimalMin("0.01") BigDecimal hourlyValue){}
 public record CatalogItem(UUID id,String name,String kind,String area,BigDecimal hourlyValue,boolean active){}
 public record CatalogResponse(List<CatalogItem> serviceTypes,List<CatalogItem> paymentTypes,List<CatalogItem> machinery,List<CatalogItem> drivers){}
 public record ServiceRequest(@Size(max=40) String protocol,AgriculturalServiceRequest.Status status,@NotNull UUID clientId,@NotNull LocalDate scheduledDate,@NotNull UUID serviceTypeId,@NotNull @DecimalMin("0.01") BigDecimal requestedHours,@NotBlank @Size(max=500) String address,LocalDate paymentDate,@Size(max=80) String funderId,UUID paymentProofTypeId,@DecimalMin("0") BigDecimal funderAmount,boolean donation,@Size(max=500) String donationOrigin){}
 public record ControlRequest(UUID machineryId,UUID tractorDriverId,@DecimalMin("0") BigDecimal initialHourMeter,@DecimalMin("0") BigDecimal finalHourMeter){}
 public record ControlResponse(UUID id,UUID machineryId,String machineryName,UUID tractorDriverId,String tractorDriverName,BigDecimal initialHourMeter,BigDecimal finalHourMeter,BigDecimal performedHours,BigDecimal remainingHours,String hoursStatus){}
 public record ServiceResponse(UUID id,String protocol,AgriculturalServiceRequest.Status status,UUID clientId,String clientName,LocalDate scheduledDate,UUID serviceTypeId,String serviceTypeName,BigDecimal requestedHours,String address,BigDecimal amount,LocalDate paymentDate,LocalDate expirationDate,String funderId,UUID paymentProofTypeId,BigDecimal funderAmount,boolean donation,String donationOrigin,boolean hasPaymentProof,ControlResponse control,LocalDateTime createdAt,LocalDateTime updatedAt){}
}
