package com.fiap.hackgov.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hackgov.DTOs.CityHall.CityHallDTO;
import com.fiap.hackgov.DTOs.CityHall.CreateCityHallDTO;
import com.fiap.hackgov.entities.CityHall;
import com.fiap.hackgov.entities.State;
import com.fiap.hackgov.infra.filters.JwtAuthenticationFilter;
import com.fiap.hackgov.infra.filters.RateLimitFilter;
import com.fiap.hackgov.infra.security.SecurityProperties;
import com.fiap.hackgov.mapper.CityHallMapper;
import com.fiap.hackgov.services.CityHallService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CityHallController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {JwtAuthenticationFilter.class, RateLimitFilter.class}
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
public class CityHallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityHallService cityHallService;

    @MockBean
    private CityHallMapper cityHallMapper;

    @MockBean
    private SecurityProperties securityProperties;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // POST /api/cityhall
    // -------------------------------------------------------------------------

    @Test
    void createCityHall_Success() throws Exception {
        UUID stateId = UUID.randomUUID();
        CreateCityHallDTO createDTO = new CreateCityHallDTO(
                "Prefeitura de SP",
                "12345678000195",
                stateId
        );

        CityHall cityHall = new CityHall();
        cityHall.setId(UUID.randomUUID());
        cityHall.setName("Prefeitura de SP");
        cityHall.setCnpj("12345678000195");

        when(cityHallService.save(any(CreateCityHallDTO.class))).thenReturn(cityHall);

        mockMvc.perform(post("/api/cityhall")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/cityhall/" + cityHall.getId()));
    }

    @Test
    void createCityHall_BlankName_ReturnsBadRequest() throws Exception {
        CreateCityHallDTO invalid = new CreateCityHallDTO("", "12345678000195", UUID.randomUUID());

        mockMvc.perform(post("/api/cityhall")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(cityHallService, never()).save(any());
    }

    @Test
    void createCityHall_InvalidCnpj_ReturnsBadRequest() throws Exception {
        CreateCityHallDTO invalid = new CreateCityHallDTO("Prefeitura de SP", "123", UUID.randomUUID());

        mockMvc.perform(post("/api/cityhall")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(cityHallService, never()).save(any());
    }

    @Test
    void createCityHall_NullStateId_ReturnsBadRequest() throws Exception {
        CreateCityHallDTO invalid = new CreateCityHallDTO("Prefeitura de SP", "12345678000195", null);

        mockMvc.perform(post("/api/cityhall")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(cityHallService, never()).save(any());
    }

    @Test
    void createCityHall_DuplicateCnpj_ReturnsConflict() throws Exception {
        CreateCityHallDTO createDTO = new CreateCityHallDTO("Prefeitura de SP", "12345678000195", UUID.randomUUID());

        when(cityHallService.save(any(CreateCityHallDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "CityHall with this CNPJ already exists"));

        mockMvc.perform(post("/api/cityhall")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCityHall_StateNotFound_ReturnsNotFound() throws Exception {
        CreateCityHallDTO createDTO = new CreateCityHallDTO("Prefeitura de SP", "12345678000195", UUID.randomUUID());

        when(cityHallService.save(any(CreateCityHallDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found"));

        mockMvc.perform(post("/api/cityhall")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/cityhall
    // -------------------------------------------------------------------------

    @Test
    void getAllCityHalls_Success() throws Exception {
        UUID cityHallId = UUID.randomUUID();

        CityHall cityHall1 = new CityHall();
        cityHall1.setId(cityHallId);
        cityHall1.setName("Prefeitura de SP");
        cityHall1.setCnpj("12345678000195");

        CityHallDTO dto1 = new CityHallDTO(
                cityHallId,
                "Prefeitura de SP",
                "12345678000195",
                "São Paulo",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Page<CityHall> page = new PageImpl<>(List.of(cityHall1), PageRequest.of(0, 10), 1);

        when(cityHallService.findAll(any(Pageable.class))).thenReturn(page);
        when(cityHallMapper.toCityHallDTO(cityHall1)).thenReturn(dto1);

        mockMvc.perform(get("/api/cityhall")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Prefeitura de SP"))
                .andExpect(jsonPath("$.content[0].cnpj").value("12345678000195"))
                .andExpect(jsonPath("$.content[0].stateName").value("São Paulo"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getAllCityHalls_EmptyPage_ReturnsOk() throws Exception {
        when(cityHallService.findAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/cityhall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /api/cityhall/{id}
    // -------------------------------------------------------------------------

    @Test
    void getCityHallById_Success() throws Exception {
        UUID cityHallId = UUID.randomUUID();

        CityHallDTO dto = new CityHallDTO(
                cityHallId,
                "Prefeitura de SP",
                "12345678000195",
                "São Paulo",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        CityHall cityHall = new CityHall();
        cityHall.setId(cityHallId);
        cityHall.setName("Prefeitura de SP");
        cityHall.setCnpj("12345678000195");
        cityHall.setState(new State());
        cityHall.getState().setName("São Paulo");

        when(cityHallService.findById(any(UUID.class))).thenReturn(cityHall);
        when(cityHallMapper.toCityHallDTO(cityHall)).thenReturn(dto);

        mockMvc.perform(get("/api/cityhall/" + cityHallId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Prefeitura de SP"))
                .andExpect(jsonPath("$.cnpj").value("12345678000195"))
                .andExpect(jsonPath("$.stateName").value("São Paulo"));
    }

    @Test
    void getCityHallById_NotFound_ReturnsNotFound() throws Exception {
        when(cityHallService.findById(any(UUID.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "CityHall not found"));

        mockMvc.perform(get("/api/cityhall/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCityHallById_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/cityhall/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}