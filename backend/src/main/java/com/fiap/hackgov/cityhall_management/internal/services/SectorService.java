package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.CreateSectorDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.SectorResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.Actions;
import com.fiap.hackgov.cityhall_management.internal.mapper.SectorMapper;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.permissions.RequiresPermission;
import com.fiap.hackgov.shared.infra.security.SecurityContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class SectorService {

    private final SectorRepository sectorRepository;
    private final SectorMapper sectorMapper;
    private final CityHallService cityHallService;
    private final SecurityContext securityContext;

    @RequiresPermission(resource = "SECTOR", action = Actions.CREATE)
    public SectorResponseDTO createSector(CreateSectorDTO sectorDTO, Employee employee) {

        var cityHall = cityHallService.findById(sectorDTO.cityHall().getId());

        if (cityHall == null) {
            throw new BusinessException("City Hall not found");
        }

        Sector sector = sectorMapper.toEntity(sectorDTO);
        sector.setCityHall(cityHall);

        sectorRepository.save(sector);

        return sectorMapper.toDTO(sector);
    }

    @RequiresPermission(resource = "SECTOR", action = Actions.READ)
    public Page<SectorResponseDTO> getAllSectors(Pageable pageable, Employee employee) {

        System.out.println("PERMISSIONS: " + securityContext.getCurrentPermissions());
        System.out.println("REQUIRED: SECTOR:READ");

        return sectorRepository
                .findAllByCityHall_Id(employee.getCityHallId().getId(), pageable)
                .map(sectorMapper::toDTO);
    }

    @RequiresPermission(resource = "SECTOR", action = Actions.READ)
    public SectorResponseDTO getById(UUID id, Employee employee) {

        Sector sector = sectorRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("Sector not found"));

        return sectorMapper.toDTO(sector);
    }

    @RequiresPermission(resource = "SECTOR", action = Actions.READ)
    public SectorResponseDTO getByName(String name, Employee employee) {

        Sector sector = sectorRepository
                .findByNameAndCityHall_Id(name, employee.getCityHallId().getId())
                .orElseThrow(() -> new BusinessException("Sector not found"));

        return sectorMapper.toDTO(sector);
    }
}
