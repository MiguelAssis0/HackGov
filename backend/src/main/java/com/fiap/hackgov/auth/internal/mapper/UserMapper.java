package com.fiap.hackgov.auth.internal.mapper;

import com.fiap.hackgov.auth.internal.DTOs.users.CreateUserDTO;
import com.fiap.hackgov.auth.internal.DTOs.users.UserDTO;
import com.fiap.hackgov.auth.internal.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toEntity(CreateUserDTO createUserDTO);

    @Mapping(target = "password", ignore = true)
    CreateUserDTO toDTO(User user);

    UserDTO toUserDTO(User user);
}
