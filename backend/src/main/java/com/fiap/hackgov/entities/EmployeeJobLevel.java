package com.fiap.hackgov.entities;

import com.fiap.hackgov.entities.embeddables.EmployeeJobLevelPK;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
public class EmployeeJobLevel implements Serializable  {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private EmployeeJobLevelPK pk = new EmployeeJobLevelPK();

    public EmployeeJobLevel(Employee employee, JobLevel jobLevel) {
        this.pk.setEmployee(employee);
        this.pk.setJobLevel(jobLevel);
    }

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
