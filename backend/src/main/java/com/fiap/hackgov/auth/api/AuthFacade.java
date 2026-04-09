package com.fiap.hackgov.auth.api;

import com.fiap.hackgov.auth.internal.DTOs.users.UserDTO;

import java.util.UUID;

// auth/api/AuthFacade.java
public interface AuthFacade {
    UserDTO findById(UUID id);
}