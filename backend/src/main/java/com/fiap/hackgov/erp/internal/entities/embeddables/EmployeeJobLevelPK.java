package com.fiap.hackgov.erp.internal.entities.embeddables;

import com.fiap.hackgov.erp.internal.entities.Employee;
import com.fiap.hackgov.erp.internal.entities.JobLevel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class EmployeeJobLevelPK implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "joblevel_id")
    private JobLevel jobLevel;

}