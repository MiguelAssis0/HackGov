package com.fiap.hackgov.cityhall_management.internal.entities;

import com.fiap.hackgov.cityhall_management.internal.entities.embeddables.PermissionsOccupationPK;
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
public class PermissionsOccupation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private PermissionsOccupationPK pk = new PermissionsOccupationPK();

    public PermissionsOccupation(Permissions permission, Occupation occupation) {
        this.pk.setPermission(permission);
        this.pk.setOccupation(occupation);
    }

}