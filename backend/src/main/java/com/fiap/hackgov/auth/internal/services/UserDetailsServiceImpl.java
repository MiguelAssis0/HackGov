package com.fiap.hackgov.auth.internal.services;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(email)
                .map(u -> (User) u)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}