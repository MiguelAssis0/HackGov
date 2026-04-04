package com.fiap.hackgov.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "job_levels_sectors")
public class JobLevelSector implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private JobLevelSectorPK pk = new JobLevelSectorPK();

    public JobLevelSector(Sector sector, JobLevel jobLevel) {
        this.pk.setSector(sector);
        this.pk.setJobLevel(jobLevel);
    }

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
