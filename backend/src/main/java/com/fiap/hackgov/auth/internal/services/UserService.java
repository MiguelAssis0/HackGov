package com.fiap.hackgov.auth.internal.services;

import com.fiap.hackgov.auth.internal.DTOs.users.CreateUserDTO;
import com.fiap.hackgov.auth.internal.DTOs.users.UserDTO;
import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.mapper.UserMapper;
import com.fiap.hackgov.auth.internal.repositories.UserRepository;
import com.fiap.hackgov.shared.infra.exceptions.EmployeeAlreadyExistsException;
import com.fiap.hackgov.shared.infra.exceptions.EmployeeNotFoundException;
import com.fiap.hackgov.shared.infra.utils.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLog auditLog;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public User save(CreateUserDTO userDTO) {
        auditLog.with(log).event("save_user").email(userDTO.email()).level(AuditLog.Level.INFO).log();

        if(userRepository.findByEmail(userDTO.email()).isPresent()){
            auditLog.with(log).event("save_user_failed").reason("email_already_exists").email(userDTO.email()).level(AuditLog.Level.WARN).log();
            throw new EmployeeAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.password()));

        auditLog.with(log).event("save_user_success").level(AuditLog.Level.INFO).log();

        return userRepository.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        auditLog.with(log).event("find_all_users").level(AuditLog.Level.INFO).log();
        return userRepository.findAll(pageable);
    }

    public UserDTO findById(UUID uuid) {
        auditLog.with(log).event("find_employee_by_id").level(AuditLog.Level.INFO).log();
        User employee = userRepository.findById(uuid)
                .orElseThrow(() -> {
                    auditLog.with(log).event("find_employee_by_id_failed").reason("employee_not_found").level(AuditLog.Level.WARN).log();
                    return new EmployeeNotFoundException("Employee not found");
                });

        auditLog.with(log).event("find_employee_by_id_success").level(AuditLog.Level.INFO).log();
        return userMapper.toUserDTO(employee);
    }
}
