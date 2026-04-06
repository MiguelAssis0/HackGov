package com.fiap.hackgov.services;

import com.fiap.hackgov.DTOs.State.CreateStateDTO;
import com.fiap.hackgov.DTOs.State.StateDTO;
import com.fiap.hackgov.entities.State;
import com.fiap.hackgov.mapper.StateMapper;
import com.fiap.hackgov.repositories.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class StateService {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private StateMapper stateMapper;

    public State save(CreateStateDTO dto) {
        stateRepository.findByUf(dto.uf()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "State with this UF already exists");
        });

        State state = new State();
        state.setName(dto.name());
        state.setUf(dto.uf());

        return stateRepository.save(state);
    }

    public Page<State> findAll(Pageable pageable) {
        return stateRepository.findAll(pageable);
    }

    public StateDTO findById(UUID id) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found"));
        return stateMapper.toStateDTO(state);
    }
}