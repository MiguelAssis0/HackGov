package com.fiap.hackgov.cityhall_management.internal.repositories;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

}
