package com.fiap.hackgov.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "job_levels_sectors")
public class JobLevelSector {

    @EmbeddedId
    private JobLevelSectorPK pk = new JobLevelSectorPK();

    public JobLevelSector(Sector sector, JobLevel jobLevel) {
        this.pk.setSector(sector);
        this.pk.setJobLevel(jobLevel);
    }

}
