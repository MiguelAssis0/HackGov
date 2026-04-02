package com.fiap.hackgov.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "permissions_job_level")
@Getter
@Setter
@NoArgsConstructor
public class PermissionsJobLevel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private PermissionsJobLevelPK pk = new PermissionsJobLevelPK();

    public PermissionsJobLevel(Permissions permission, JobLevel jobLevel) {
        this.pk.setPermission(permission);
        this.pk.setJobLevel(jobLevel);
    }

}