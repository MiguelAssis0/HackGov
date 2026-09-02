package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.CreateSectorDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Sector.SectorResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.mapper.SectorMapper;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.text.Normalizer;
import java.util.Locale;

@Service
@AllArgsConstructor
public class SectorService {

    private final SectorRepository sectorRepository;
    private final SectorMapper sectorMapper;

    @Transactional
    public SectorResponseDTO createSector(CreateSectorDTO sectorDTO, Employee employee) {
        Employee current = admin(employee);
        var cityHall = current.getCityHallId();
        String name = validName(sectorDTO.name());
        String slug = validSlug(sectorDTO.slug(), name);
        ensureUnique(cityHall.getId(), slug, null);
        Sector sector = new Sector();
        sector.setName(name);
        sector.setSlug(slug);
        sector.setDescription(description(sectorDTO.description()));
        sector.setActive(sectorDTO.active() == null || sectorDTO.active());
        sector.setCityHall(cityHall);
        return sectorMapper.toDTO(sectorRepository.save(sector));
    }

    @Transactional(readOnly = true)
    public Page<SectorResponseDTO> getAllSectors(Pageable pageable, Employee employee) {
        return sectorRepository
                .findAllByCityHall_Id(city(employee), pageable)
                .map(sectorMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public SectorResponseDTO getById(UUID id, Employee employee) {

        Sector sector = sectorRepository
                .findByIdAndCityHall_Id(id, city(employee))
                .orElseThrow(() -> new ResourceNotFoundException("Setor nao encontrado"));

        return sectorMapper.toDTO(sector);
    }

    @Transactional(readOnly = true)
    public SectorResponseDTO getByName(String name, Employee employee) {

        Sector sector = sectorRepository
                .findByNameAndCityHall_Id(name, city(employee))
                .orElseThrow(() -> new ResourceNotFoundException("Setor nao encontrado"));

        return sectorMapper.toDTO(sector);
    }

    @Transactional
    public SectorResponseDTO updateSector(UUID id, CreateSectorDTO dto, Employee employee) {
        Employee current = admin(employee);
        UUID cityHallId = city(current);
        Sector sector = sectorRepository.findByIdAndCityHall_Id(id, cityHallId)
                .orElseThrow(() -> new ResourceNotFoundException("Setor nao encontrado"));
        String name = validName(dto.name());
        String slug = validSlug(dto.slug(), name);
        ensureUnique(cityHallId, slug, id);
        sector.setName(name);
        sector.setSlug(slug);
        sector.setDescription(description(dto.description()));
        if (dto.active() != null) sector.setActive(dto.active());
        return sectorMapper.toDTO(sectorRepository.save(sector));
    }

    @Transactional
    public SectorResponseDTO toggleSector(UUID id, Employee employee) {
        Employee current = admin(employee);
        Sector sector = sectorRepository.findByIdAndCityHall_Id(id, city(current))
                .orElseThrow(() -> new ResourceNotFoundException("Setor nao encontrado"));
        sector.setActive(!sector.isActive());
        return sectorMapper.toDTO(sectorRepository.save(sector));
    }

    private void ensureUnique(UUID cityHallId, String slug, UUID excludedId) {
        boolean exists = excludedId == null
                ? sectorRepository.existsByCityHall_IdAndSlug(cityHallId, slug)
                : sectorRepository.existsByCityHall_IdAndSlugAndIdNot(cityHallId, slug, excludedId);
        if (exists) throw new BusinessException("Ja existe um setor com este identificador nesta prefeitura");
    }

    private String validName(String value) {
        String name = value == null ? "" : value.trim();
        if (name.isBlank()) throw new BusinessException("Informe o nome do setor");
        if (name.length() > 120) throw new BusinessException("O nome do setor deve ter no maximo 120 caracteres");
        return name;
    }

    private String validSlug(String value, String name) {
        String slug = value == null || value.isBlank() ? slugify(name) : value.trim().toLowerCase(Locale.ROOT);
        if (slug.isBlank()) throw new BusinessException("Informe um nome para gerar o identificador");
        if (slug.length() > 140 || !slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
            throw new BusinessException("O identificador deve conter apenas letras, numeros e hifens");
        return slug;
    }

    private String slugify(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String description(String value) {
        return value == null ? "" : value.trim();
    }

    private Employee admin(Employee employee) {
        if (employee == null || employee.getCityHallId() == null)
            throw new UnauthorizedException("O usuario precisa estar vinculado a uma prefeitura");
        if (!Roles.ADMIN.equals(employee.getRole()))
            throw new UnauthorizedException("Somente administradores podem gerenciar setores");
        return employee;
    }

    private UUID city(Employee employee) {
        if (employee == null || employee.getCityHallId() == null)
            throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee.getCityHallId().getId();
    }
}
