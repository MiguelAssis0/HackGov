package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.State;
import com.fiap.hackgov.cityhall_management.internal.repositories.StateRepository;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StateService {

    @Autowired
    private StateRepository stateRepository;

    public Page<State> findAll(Pageable pageable) {
        return stateRepository.findAll(pageable);
    }

    public State findById(UUID id) {
        return stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("State not found: " + id));
    }
}