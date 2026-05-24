package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation.CreateOccupationDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation.OccupationResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.mapper.OccupationMapper;
import com.fiap.hackgov.cityhall_management.internal.repositories.OccupationRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.Actions;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.permissions.RequiresPermission;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OccupationService {

    private final OccupationRepository occupationRepository;
    private final OccupationMapper occupationMapper;
    private final SectorRepository sectorRepository;

    @RequiresPermission(resource = "SECTOR", action = Actions.CREATE)
    public OccupationResponseDTO createOccupation(CreateOccupationDTO dto, Employee employee) {
        Sector sector = sectorRepository
                .findByIdAndCityHall_Id(dto.sectorId(), employee.getCityHallId().getId())
                .orElseThrow(() -> new BusinessException("Setor nao encontrado para a prefeitura do usuario autenticado"));

        Occupation occupation = occupationMapper.toEntity(dto);
        occupation.setSectorId(sector);

        return occupationMapper.toDTO(occupationRepository.save(occupation));
    }

    @RequiresPermission(resource = "SECTOR", action = Actions.READ)
    public Page<OccupationResponseDTO> getAllOccupations(Pageable pageable, Employee employee) {
        return occupationRepository
                .findAllBySectorId_CityHall_Id(employee.getCityHallId().getId(), pageable)
                .map(occupationMapper::toDTO);
    }
}
