package com.fiap.hackgov.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions_job_level")
@Getter
@Setter
@NoArgsConstructor
public class PermissionsJobLevel {

    @EmbeddedId
    private PermissionsJobLevelPK pk = new PermissionsJobLevelPK();

    public PermissionsJobLevel(Permissions permission, JobLevel jobLevel) {
        this.pk.setPermission(permission);
        this.pk.setJobLevel(jobLevel);
    }

}