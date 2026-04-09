package com.fiap.hackgov.auth.internal.facade;

import com.fiap.hackgov.auth.api.AuthFacade;
import com.fiap.hackgov.auth.internal.DTOs.users.UserDTO;
import com.fiap.hackgov.auth.internal.mapper.UserMapper;
import com.fiap.hackgov.auth.internal.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

// auth/internal/facade/AuthFacadeImpl.java
@Service
class AuthFacadeImpl implements AuthFacade {

    private final UserRepository userRepository;

    @Autowired
    UserMapper userMapper;

    AuthFacadeImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toUserDTO)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
