package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.DTOs.CityHall.CreateCityHallDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.State;
import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.StateRepository;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class CityHallService {

    @Autowired
    private CityHallRepository cityHallRepository;

    @Autowired
    private StateRepository stateRepository;

    public CityHall save(CreateCityHallDTO dto) {

        cityHallRepository.findByCnpj(dto.cnpj()).ifPresent(cityHall -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CityHall with this CNPJ already exists");
        });

        State state = stateRepository.findById(dto.stateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found: " + dto.stateId()));

        CityHall cityHall = new CityHall();
        cityHall.setName(dto.name());
        cityHall.setCnpj(dto.cnpj());
        cityHall.setState(state);

        return cityHallRepository.save(cityHall);
    }

    public Page<CityHall> findAll(Pageable pageable) {
        return cityHallRepository.findAll(pageable);
    }

    public CityHall findById(UUID id) {
        return cityHallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CityHall not found: " + id));
    }
}