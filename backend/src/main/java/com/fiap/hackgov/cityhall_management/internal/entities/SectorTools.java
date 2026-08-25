package com.fiap.hackgov.cityhall_management.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.embeddables.SectorToolsPK;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "sector_tools")
@Getter
@Setter
@NoArgsConstructor
@Filter(name = "cityHallFilter", condition = "sector_id in (select s.id from sectors s where s.city_hall_id = :cityHallId)")
@Filter(name = "sectorFilter", condition = "sector_id = :sectorId")
public class SectorTools implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private SectorToolsPK pk = new SectorToolsPK();

    public SectorTools(Sector sector, Tools tools) {
        this.pk.setSector(sector);
        this.pk.setTools(tools);
    }

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
