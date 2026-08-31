package com.fiap.hackgov.agriculture.internal.repositories;

import com.fiap.hackgov.agriculture.internal.entities.AgriculturalServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AgriculturalServiceRequestRepository extends JpaRepository<AgriculturalServiceRequest, UUID> {
    @EntityGraph(attributePaths = {"client", "serviceType", "paymentProofType"})
    @Query("select s from AgriculturalServiceRequest s where s.cityHall.id=:cityId and (:query='' or lower(s.client.fullName) like lower(concat('%',:query,'%')) or lower(s.protocol) like lower(concat('%',:query,'%'))) order by s.scheduledDate desc,s.createdAt desc")
    Page<AgriculturalServiceRequest> search(@Param("cityId") UUID cityId, @Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"client", "serviceType", "paymentProofType", "cityHall"})
    Optional<AgriculturalServiceRequest> findByIdAndCityHall_Id(UUID id, UUID cityId);
}
