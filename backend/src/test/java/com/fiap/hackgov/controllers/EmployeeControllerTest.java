package com.fiap.hackgov.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hackgov.DTOs.Employee.CreateEmployeeDTO;
import com.fiap.hackgov.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.entities.Employee;
import com.fiap.hackgov.entities.enums.Roles;
import com.fiap.hackgov.mapper.EmployeeMapper;
import com.fiap.hackgov.services.AuthService;
import com.fiap.hackgov.services.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = EmployeeController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {com.fiap.hackgov.infra.security.Filter.class}
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private EmployeeMapper employeeMapper;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEmployee_Success() throws Exception {
        UUID cityHallId = UUID.randomUUID();
        CreateEmployeeDTO employeeDTO = new CreateEmployeeDTO(
                "Test User",
                "52998224725",
                "test@example.com",
                "password123",
                true,
                Roles.EMPLOYEE,
                null,
                "1234567890",
                false,
                cityHallId
        );

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setName("Test User");
        employee.setEmail("test@example.com");

        when(employeeService.save(any(CreateEmployeeDTO.class))).thenReturn(employee);

        mockMvc.perform(post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employee/" + employee.getId()));
    }

    @Test
    void createEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        UUID cityHallId = UUID.randomUUID();
        CreateEmployeeDTO invalidDTO = new CreateEmployeeDTO(
                "",
                "12345678901",
                "test@example.com",
                "password123",
                true,
                Roles.EMPLOYEE,
                null,
                "1234567890",
                false,
                cityHallId
        );

        mockMvc.perform(post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllEmployees_Success() throws Exception {
        Employee employee1 = new Employee();
        employee1.setId(UUID.randomUUID());
        employee1.setName("User 1");
        employee1.setCpf("12345678901");
        employee1.setEmail("user1@example.com");

        Employee employee2 = new Employee();
        employee2.setId(UUID.randomUUID());
        employee2.setName("User 2");
        employee2.setCpf("98765432100");
        employee2.setEmail("user2@example.com");

        EmployeeDTO dto1 = new EmployeeDTO(employee1.getId(), "User 1", "12345678901",
                "user1@example.com", true, Roles.EMPLOYEE, null, "1234567890",
                false, UUID.randomUUID(), null, null, null, null, null, null, null);

        EmployeeDTO dto2 = new EmployeeDTO(employee2.getId(), "User 2", "98765432100",
                "user2@example.com", true, Roles.EMPLOYEE, null, "1234567890",
                false, UUID.randomUUID(), null, null, null, null, null, null, null);

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee1, employee2), PageRequest.of(0, 10), 2
        );

        when(employeeService.findAll(any(Pageable.class)))
                .thenReturn(employeePage);

        // mockar o mapper para cada employee
        when(employeeMapper.toEmployeeDTO(employee1)).thenReturn(dto1);
        when(employeeMapper.toEmployeeDTO(employee2)).thenReturn(dto2);

        mockMvc.perform(get("/api/employee")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("User 1"))
                .andExpect(jsonPath("$.content[1].name").value("User 2"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        EmployeeDTO employee = new EmployeeDTO(
                UUID.randomUUID(),
                "Test User",
                "52998224725",
                "test@example.com",
                true,
                Roles.EMPLOYEE,
                null,
                "1234567890",
                false,
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(employeeService.findById(any(String.class), any(jakarta.servlet.http.HttpServletRequest.class)))
                .thenReturn(employee);

        mockMvc.perform(get("/api/employee/" + employee.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void getEmployeeById_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/employee/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}