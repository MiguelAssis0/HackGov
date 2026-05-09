package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.CreateSectorDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.SectorResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.mapper.SectorMapper;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.services.VerificationService;
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
    private final VerificationService verificationService;

    public SectorResponseDTO createSector(CreateSectorDTO sectorDTO, Employee employee) {

        var cityHall = cityHallService.findById(sectorDTO.cityHall().getId());

        if (cityHall == null) {
            throw new BusinessException("City Hall not found");
        }

        verificationService.checkCityHallAccess(employee, cityHall);
        verificationService.checkPermission(employee, "CREATE_SECTOR");

        Sector sector = sectorMapper.toEntity(sectorDTO);
        sector.setCityHall(cityHall);

        sectorRepository.save(sector);

        return sectorMapper.toDTO(sector);
    }

    public Page<SectorResponseDTO> getAllSectors(Pageable pageable, Employee employee) {

        verificationService.checkPermission(employee, "VIEW_SECTORS");

        return sectorRepository
                .findAllByCityHall_Id(employee.getCityHallId().getId(), pageable)
                .map(sectorMapper::toDTO);
    }

    public SectorResponseDTO getById(UUID id, Employee employee) {



        verificationService.checkPermission(employee, "VIEW_SECTORS");

        Sector sector = sectorRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("Sector not found"));

        return sectorMapper.toDTO(sector);
    }

    public SectorResponseDTO getByName(String name, Employee employee) {

        verificationService.checkPermission(employee, "VIEW_SECTORS");

        Sector sector = sectorRepository
                .findByNameAndCityHall_Id(name, employee.getCityHallId().getId())
                .orElseThrow(() -> new BusinessException("Sector not found"));

        return sectorMapper.toDTO(sector);
    }
}
