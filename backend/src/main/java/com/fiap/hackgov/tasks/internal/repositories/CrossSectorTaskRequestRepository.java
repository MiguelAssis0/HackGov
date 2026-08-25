package com.fiap.hackgov.tasks.internal.repositories;
import com.fiap.hackgov.tasks.internal.entities.CrossSectorTaskRequest;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface CrossSectorTaskRequestRepository extends JpaRepository<CrossSectorTaskRequest,UUID>{Optional<CrossSectorTaskRequest> findByIdAndCityHall_Id(UUID id,UUID cityHallId);List<CrossSectorTaskRequest> findByCityHall_IdOrderByCreatedAtDesc(UUID cityHallId);}
