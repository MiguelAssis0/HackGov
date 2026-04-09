package com.fiap.hackgov.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hackgov.erp.internal.DTOs.State.CreateStateDTO;
import com.fiap.hackgov.erp.internal.DTOs.State.StateDTO;
import com.fiap.hackgov.erp.internal.controllers.StateController;
import com.fiap.hackgov.erp.internal.entities.State;
import com.fiap.hackgov.erp.internal.entities.enums.UF;
import com.fiap.hackgov.shared.infra.filters.JwtAuthenticationFilter;
import com.fiap.hackgov.shared.infra.filters.RateLimitFilter;
import com.fiap.hackgov.shared.infra.security.SecurityProperties;
import com.fiap.hackgov.erp.internal.mapper.StateMapper;
import com.fiap.hackgov.erp.internal.services.StateService;
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
        controllers = StateController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {JwtAuthenticationFilter.class, RateLimitFilter.class}
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
public class StateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StateService stateService;

    @MockBean
    private StateMapper stateMapper;

    @MockBean
    private SecurityProperties securityProperties;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // POST /api/state
    // -------------------------------------------------------------------------

    @Test
    void createState_Success() throws Exception {
        CreateStateDTO createDTO = new CreateStateDTO("São Paulo", UF.SP);

        State state = new State();
        state.setId(UUID.randomUUID());
        state.setName("São Paulo");
        state.setUf(UF.SP);

        when(stateService.save(any(CreateStateDTO.class))).thenReturn(state);

        mockMvc.perform(post("/api/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/state/" + state.getId()));
    }

    @Test
    void createState_BlankName_ReturnsBadRequest() throws Exception {
        CreateStateDTO invalid = new CreateStateDTO("", UF.SP);

        mockMvc.perform(post("/api/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(stateService, never()).save(any());
    }

    @Test
    void createState_NullUf_ReturnsBadRequest() throws Exception {
        CreateStateDTO invalid = new CreateStateDTO("São Paulo", null);

        mockMvc.perform(post("/api/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(stateService, never()).save(any());
    }

    @Test
    void createState_DuplicateUf_ReturnsConflict() throws Exception {
        CreateStateDTO createDTO = new CreateStateDTO("São Paulo", UF.SP);

        when(stateService.save(any(CreateStateDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "State with this UF already exists"));

        mockMvc.perform(post("/api/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------------------------
    // GET /api/state
    // -------------------------------------------------------------------------

    @Test
    void getAllStates_Success() throws Exception {
        UUID stateId = UUID.randomUUID();

        State state = new State();
        state.setId(stateId);
        state.setName("São Paulo");
        state.setUf(UF.SP);

        StateDTO dto = new StateDTO(stateId, "São Paulo", UF.SP, LocalDateTime.now(), LocalDateTime.now());

        Page<State> page = new PageImpl<>(List.of(state), PageRequest.of(0, 10), 1);

        when(stateService.findAll(any(Pageable.class))).thenReturn(page);
        when(stateMapper.toStateDTO(state)).thenReturn(dto);

        mockMvc.perform(get("/api/state")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("São Paulo"))
                .andExpect(jsonPath("$.content[0].uf").value("SP"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getAllStates_EmptyPage_ReturnsOk() throws Exception {
        when(stateService.findAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /api/state/{id}
    // -------------------------------------------------------------------------

    @Test
    void getStateById_Success() throws Exception {
        UUID stateId = UUID.randomUUID();

        State state = new State();
        state.setId(stateId);
        state.setName("São Paulo");
        state.setUf(UF.SP);
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());

        StateDTO stateDTO = new StateDTO(stateId, "São Paulo", UF.SP, LocalDateTime.now(), LocalDateTime.now());
        
        when(stateService.findById(any(UUID.class))).thenReturn(state);
        when(stateMapper.toStateDTO(state)).thenReturn(stateDTO);

        mockMvc.perform(get("/api/state/" + stateId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("São Paulo"))
                .andExpect(jsonPath("$.uf").value("SP"));
    }

    @Test
    void getStateById_NotFound_ReturnsNotFound() throws Exception {
        when(stateService.findById(any(UUID.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found"));

        mockMvc.perform(get("/api/state/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStateById_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/state/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}