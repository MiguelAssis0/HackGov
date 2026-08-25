package com.fiap.hackgov.bidding.internal.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "etps")
@Filter(name = "cityHallFilter", condition = BiddingScopeConditions.REQ_CHILD_CITY)
@Filter(name = "sectorFilter", condition = BiddingScopeConditions.REQ_CHILD_SECTOR)
public class ETP {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "requisition_id")
    private Requisition requisition;
    private String content;
}
