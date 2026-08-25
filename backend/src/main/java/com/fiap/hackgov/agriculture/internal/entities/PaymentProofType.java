package com.fiap.hackgov.agriculture.internal.entities;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;import jakarta.persistence.*;import lombok.*;import java.util.UUID;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="agricultural_payment_types",uniqueConstraints=@UniqueConstraint(name="agri_payment_city_name_uk",columnNames={"city_hall_id","name"}))
public class PaymentProofType { @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="city_hall_id") private CityHall cityHall; @Column(nullable=false,length=100) private String name; private boolean active=true; }
