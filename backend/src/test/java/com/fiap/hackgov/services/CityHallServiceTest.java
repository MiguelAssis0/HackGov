package com.fiap.hackgov.services;

import com.fiap.hackgov.erp.internal.DTOs.CityHall.CityHallDTO;
import com.fiap.hackgov.erp.internal.DTOs.CityHall.CreateCityHallDTO;
import com.fiap.hackgov.erp.internal.services.CityHallService;
import com.fiap.hackgov.erp.internal.entities.CityHall;
import com.fiap.hackgov.erp.internal.entities.State;
import com.fiap.hackgov.erp.internal.mapper.CityHallMapper;
import com.fiap.hackgov.erp.internal.repositories.CityHallRepository;
import com.fiap.hackgov.erp.internal.repositories.StateRepository;
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
public class CityHallServiceTest {

    @Mock
    private CityHallRepository cityHallRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private CityHallMapper cityHallMapper;

    @InjectMocks
    private CityHallService cityHallService;

    private UUID cityHallId;
    private UUID stateId;
    private State state;
    private CityHall cityHall;
    private CreateCityHallDTO createDTO;
    private CityHallDTO cityHallDTO;

    @BeforeEach
    void setUp() {
        cityHallId = UUID.randomUUID();
        stateId = UUID.randomUUID();

        state = new State();
        state.setId(stateId);
        state.setName("São Paulo");

        cityHall = new CityHall();
        cityHall.setId(cityHallId);
        cityHall.setName("Prefeitura de SP");
        cityHall.setCnpj("12345678000195");
        cityHall.setState(state);

        createDTO = new CreateCityHallDTO("Prefeitura de SP", "12345678000195", stateId);

        cityHallDTO = new CityHallDTO(
                cityHallId,
                "Prefeitura de SP",
                "12345678000195",
                "São Paulo",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should create and return a CityHall when data is valid")
        void shouldCreateCityHall_whenDataIsValid() {
            when(cityHallRepository.findByCnpj(createDTO.cnpj())).thenReturn(Optional.empty());
            when(stateRepository.findById(stateId)).thenReturn(Optional.of(state));
            when(cityHallRepository.save(any(CityHall.class))).thenReturn(cityHall);

            CityHall result = cityHallService.save(createDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Prefeitura de SP");
            assertThat(result.getCnpj()).isEqualTo("12345678000195");
            assertThat(result.getState()).isEqualTo(state);

            verify(cityHallRepository).findByCnpj(createDTO.cnpj());
            verify(stateRepository).findById(stateId);
            verify(cityHallRepository).save(any(CityHall.class));
        }

        @Test
        @DisplayName("should throw 409 CONFLICT when CNPJ already exists")
        void shouldThrowConflict_whenCnpjAlreadyExists() {
            when(cityHallRepository.findByCnpj(createDTO.cnpj())).thenReturn(Optional.of(cityHall));

            assertThatThrownBy(() -> cityHallService.save(createDTO))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(rse.getReason()).isEqualTo("CityHall with this CNPJ already exists");
                    });

            verify(cityHallRepository).findByCnpj(createDTO.cnpj());
            verify(stateRepository, never()).findById(any());
            verify(cityHallRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw 404 NOT_FOUND when State does not exist")
        void shouldThrowNotFound_whenStateDoesNotExist() {
            when(cityHallRepository.findByCnpj(createDTO.cnpj())).thenReturn(Optional.empty());
            when(stateRepository.findById(stateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cityHallService.save(createDTO))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(rse.getReason()).isEqualTo("State not found");
                    });

            verify(cityHallRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return a page of CityHalls")
        void shouldReturnPageOfCityHalls() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CityHall> page = new PageImpl<>(List.of(cityHall));

            when(cityHallRepository.findAll(pageable)).thenReturn(page);

            Page<CityHall> result = cityHallService.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0)).isEqualTo(cityHall);

            verify(cityHallRepository).findAll(pageable);
        }

        @Test
        @DisplayName("should return an empty page when no CityHalls exist")
        void shouldReturnEmptyPage_whenNoCityHallsExist() {
            Pageable pageable = PageRequest.of(0, 10);
            when(cityHallRepository.findAll(pageable)).thenReturn(Page.empty());

            Page<CityHall> result = cityHallService.findAll(pageable);

            assertThat(result).isEmpty();
            verify(cityHallRepository).findAll(pageable);
        }
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return CityHallDTO when ID exists")
        void shouldReturnCityHall_whenIdExists() {
            when(cityHallRepository.findById(cityHallId)).thenReturn(Optional.of(cityHall));

            CityHall result = cityHallService.findById(cityHallId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(cityHallId);
            assertThat(result.getName()).isEqualTo("Prefeitura de SP");
            assertThat(result.getState().getName()).isEqualTo("São Paulo");

            verify(cityHallRepository).findById(cityHallId);
        }

        @Test
        @DisplayName("should throw 404 NOT_FOUND when ID does not exist")
        void shouldThrowNotFound_whenIdDoesNotExist() {
            when(cityHallRepository.findById(cityHallId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cityHallService.findById(cityHallId))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(rse.getReason()).isEqualTo("CityHall not found");
                    });

            verify(cityHallMapper, never()).toCityHallDTO(any());
        }
    }
}