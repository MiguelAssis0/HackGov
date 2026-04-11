package com.fiap.hackgov.erp.internal.facade;

import com.fiap.hackgov.erp.internal.DTOs.Employee.CreateUserRequestDTO;
import com.fiap.hackgov.erp.internal.DTOs.Employee.EmployeeDTO;
import com.fiap.hackgov.erp.internal.contracts.AuthFacade;
import com.fiap.hackgov.shared.infra.exceptions.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.UUID;

@Service
class AuthFacadeHttpImpl implements AuthFacade {

    @Autowired
    @Qualifier("authRestClient")
    private RestClient restClient;

    @Override
    public EmployeeDTO findById(UUID id) {
        return restClient.get()
                .uri("/auth/users/{id}", id)
                .retrieve()
                .body(EmployeeDTO.class);
    }


    @Override
    public UUID createUser(CreateUserRequestDTO request) {
        System.out.println(request);
        var response = restClient.post()
                .uri("/auth/users")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new AuthException("Error creating user (4xx)");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new AuthException("Auth service unavailable (5xx)");
                })
                .toBodilessEntity();

        String location = Objects.requireNonNull(response.getHeaders().getLocation()).toString();

        String id = location.substring(location.lastIndexOf("/") + 1);

        return UUID.fromString(id);
    }

    @Override
    public void deleteUser(UUID id) {
        restClient.delete()
                .uri("/auth/user/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
