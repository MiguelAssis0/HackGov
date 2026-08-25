package com.fiap.hackgov.agriculture.internal.entities;
import com.fiap.hackgov.cityhall_management.internal.entities.*;import com.fiap.hackgov.clients.internal.entities.Client;import com.fiap.hackgov.shared.infra.security.SensitiveStringConverter;import jakarta.persistence.*;import lombok.*;import org.hibernate.annotations.CreationTimestamp;import org.hibernate.annotations.UpdateTimestamp;import java.math.BigDecimal;import java.time.*;import java.util.UUID;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="agricultural_service_requests",uniqueConstraints=@UniqueConstraint(name="agri_protocol_city_uk",columnNames={"city_hall_id","protocol"}))
public class AgriculturalServiceRequest {
 public enum Status{CANCELLED,COMPLETED,PENDING,EXPIRED,UNPAID,IN_DEBT}
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="city_hall_id") private CityHall cityHall;
 @Column(length=40) private String protocol; @Enumerated(EnumType.STRING) private Status status=Status.PENDING; @ManyToOne(optional=false,fetch=FetchType.LAZY) private Client client;
 private LocalDate scheduledDate; @ManyToOne(optional=false,fetch=FetchType.LAZY) private AgriculturalServiceType serviceType; @Column(precision=7,scale=2) private BigDecimal requestedHours;
 @Convert(converter=SensitiveStringConverter.class) @Column(length=1000) private String address; @Column(precision=12,scale=2) private BigDecimal amount; private LocalDate paymentDate;
 @Column(length=80) private String funderId=""; @ManyToOne(fetch=FetchType.LAZY) private PaymentProofType paymentProofType; @Column(precision=12,scale=2) private BigDecimal funderAmount;
 private boolean donation; @Convert(converter=SensitiveStringConverter.class) @Column(length=1000) private String donationOrigin="";
 @Lob @Basic(fetch=FetchType.LAZY) private byte[] paymentProof; @Column(length=255) private String paymentProofName=""; @Column(length=120) private String paymentProofContentType="";
 @ManyToOne(fetch=FetchType.LAZY) private Employee createdBy; @CreationTimestamp private LocalDateTime createdAt; @UpdateTimestamp private LocalDateTime updatedAt;
 public LocalDate expirationDate(){return paymentDate==null?null:paymentDate.plusMonths(6);} public boolean shouldExpire(LocalDate today){return expirationDate()!=null&&!expirationDate().isAfter(today)&&status!=Status.CANCELLED&&status!=Status.EXPIRED;}
}
