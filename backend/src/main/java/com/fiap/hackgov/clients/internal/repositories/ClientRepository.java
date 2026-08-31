package com.fiap.hackgov.clients.internal.repositories;

import com.fiap.hackgov.clients.internal.entities.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    @Query("select c from Client c where c.cityHall.id=:cityId and (:query='' or lower(c.fullName) like lower(concat('%',:query,'%')) or lower(c.nickname) like lower(concat('%',:query,'%'))) order by c.fullName")
    Page<Client> search(@Param("cityId") UUID cityId, @Param("query") String query, Pageable pageable);

    Optional<Client> findByIdAndCityHall_Id(UUID id, UUID cityId);

    Optional<Client> findByCityHall_IdAndCpfLookup(UUID cityId, String lookup);
}
