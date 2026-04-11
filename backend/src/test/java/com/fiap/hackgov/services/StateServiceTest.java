package com.fiap.hackgov.services;

import com.fiap.hackgov.cityhall_management.internal.DTOs.State.StateDTO;
import com.fiap.hackgov.cityhall_management.internal.services.StateService;
import com.fiap.hackgov.cityhall_management.internal.entities.State;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.UF;
import com.fiap.hackgov.cityhall_management.internal.mapper.StateMapper;
import com.fiap.hackgov.cityhall_management.internal.repositories.StateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StateServiceTest {

    @Mock
    private StateRepository stateRepository;

    @Mock
    private StateMapper stateMapper;

    @InjectMocks
    private StateService stateService;

    private UUID stateId;
    private State state;
    private CreateStateDTO createDTO;
    private StateDTO stateDTO;

    @BeforeEach
    void setUp() {
        stateId = UUID.randomUUID();

        state = new State();
        state.setId(stateId);
        state.setName("São Paulo");
        state.setUf(UF.SP);

        createDTO = new CreateStateDTO("São Paulo", UF.SP);

        stateDTO = new StateDTO(stateId, "São Paulo", UF.SP, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should create and return a State when data is valid")
        void shouldCreateState_whenDataIsValid() {
            when(stateRepository.findByUf(createDTO.uf())).thenReturn(Optional.empty());
            when(stateRepository.save(any(State.class))).thenReturn(state);

            State result = stateService.save(createDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("São Paulo");
            assertThat(result.getUf()).isEqualTo(UF.SP);

            verify(stateRepository).findByUf(createDTO.uf());
            verify(stateRepository).save(any(State.class));
        }

        @Test
        @DisplayName("should throw 409 CONFLICT when UF already exists")
        void shouldThrowConflict_whenUfAlreadyExists() {
            when(stateRepository.findByUf(createDTO.uf())).thenReturn(Optional.of(state));

            assertThatThrownBy(() -> stateService.save(createDTO))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(rse.getReason()).isEqualTo("State with this UF already exists");
                    });

            verify(stateRepository).findByUf(createDTO.uf());
            verify(stateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return a page of States")
        void shouldReturnPageOfStates() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<State> page = new PageImpl<>(List.of(state));

            when(stateRepository.findAll(pageable)).thenReturn(page);

            Page<State> result = stateService.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0)).isEqualTo(state);

            verify(stateRepository).findAll(pageable);
        }

        @Test
        @DisplayName("should return an empty page when no States exist")
        void shouldReturnEmptyPage_whenNoStatesExist() {
            Pageable pageable = PageRequest.of(0, 10);
            when(stateRepository.findAll(pageable)).thenReturn(Page.empty());

            Page<State> result = stateService.findAll(pageable);

            assertThat(result).isEmpty();
            verify(stateRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return StateDTO when ID exists")
        void shouldReturnState_whenIdExists() {
            when(stateRepository.findById(stateId)).thenReturn(Optional.of(state));

            State result = stateService.findById(stateId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(stateId);
            assertThat(result.getName()).isEqualTo("São Paulo");
            assertThat(result.getUf()).isEqualTo(UF.SP);

            verify(stateRepository).findById(stateId);
        }

        @Test
        @DisplayName("should throw 404 NOT_FOUND when ID does not exist")
        void shouldThrowNotFound_whenIdDoesNotExist() {
            when(stateRepository.findById(stateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stateService.findById(stateId))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(rse.getReason()).isEqualTo("State not found");
                    });

            verify(stateMapper, never()).toStateDTO(any());
        }
    }
}