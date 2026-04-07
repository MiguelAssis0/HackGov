package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.CityHall.CreateCityHallDTO;
import com.fiap.hackgov.entities.CityHall;
import com.fiap.hackgov.entities.State;
import com.fiap.hackgov.repositories.CityHallRepository;
import com.fiap.hackgov.repositories.StateRepository;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found"));

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CityHall not found"));
    }
}