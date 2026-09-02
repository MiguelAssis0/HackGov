package com.fiap.hackgov.cityhall_management.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation.CreateOccupationDTO;
import com.fiap.hackgov.cityhall_management.internal.DTOs.Occupation.OccupationResponseDTO;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.LevelOccupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.TypeJobLevel;
import com.fiap.hackgov.cityhall_management.internal.mapper.OccupationMapper;
import com.fiap.hackgov.cityhall_management.internal.repositories.OccupationRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
@Service
@AllArgsConstructor
public class OccupationService {

    private final OccupationRepository occupationRepository;
    private final OccupationMapper occupationMapper;
    private final SectorRepository sectorRepository;

    @Transactional
    public OccupationResponseDTO createOccupation(CreateOccupationDTO dto, Employee employee) {
        Employee current = admin(employee);
        UUID cityHallId = city(current);
        String name = validName(dto.name());
        String slug = validSlug(dto.slug(), name);
        Sector sector = sector(dto.sectorId(), cityHallId);
        ensureUnique(cityHallId, sector, slug, null);

        Occupation occupation = new Occupation();
        apply(occupation, dto, name, slug, sector, true);
        occupation.setCityHall(current.getCityHallId());

        return occupationMapper.toDTO(occupationRepository.save(occupation));
    }

    @Transactional(readOnly = true)
    public Page<OccupationResponseDTO> getAllOccupations(Pageable pageable, Employee employee) {
        Employee current = admin(employee);
        return occupationRepository
                .findAllByCityHall_Id(city(current), pageable)
                .map(occupationMapper::toDTO);
    }

    @Transactional
    public OccupationResponseDTO updateOccupation(UUID id, CreateOccupationDTO dto, Employee employee) {
        Employee current = admin(employee);
        UUID cityHallId = city(current);
        Occupation occupation = occupationRepository.findByIdAndCityHall_Id(id, cityHallId)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo nao encontrado"));
        String name = validName(dto.name());
        String slug = validSlug(dto.slug(), name);
        Sector sector = sector(dto.sectorId(), cityHallId);
        ensureUnique(cityHallId, sector, slug, id);
        apply(occupation, dto, name, slug, sector, false);
        return occupationMapper.toDTO(occupationRepository.save(occupation));
    }

    @Transactional
    public OccupationResponseDTO toggleOccupation(UUID id, Employee employee) {
        Employee current = admin(employee);
        Occupation occupation = occupationRepository.findByIdAndCityHall_Id(id, city(current))
                .orElseThrow(() -> new ResourceNotFoundException("Cargo nao encontrado"));
        occupation.setActive(!occupation.isActive());
        return occupationMapper.toDTO(occupationRepository.save(occupation));
    }

    private void apply(Occupation occupation, CreateOccupationDTO dto, String name, String slug, Sector sector, boolean creating) {
        occupation.setName(name);
        occupation.setSlug(slug);
        occupation.setDescription(dto.description() == null ? "" : dto.description().trim());
        occupation.setTypes(dto.types() == null ? (creating ? TypeJobLevel.CONCURSADO : occupation.getTypes()) : dto.types());
        occupation.setLevel(dto.level() == null ? (creating ? LevelOccupation.JUNIOR : occupation.getLevel()) : dto.level());
        occupation.setSectorId(sector);
        if (creating || dto.active() != null) occupation.setActive(dto.active() == null || dto.active());
    }

    private Sector sector(UUID id, UUID cityHallId) {
        if (id == null) return null;
        return sectorRepository.findByIdAndCityHall_Id(id, cityHallId)
                .orElseThrow(() -> new BusinessException("Setor invalido para a prefeitura do usuario autenticado"));
    }

    private void ensureUnique(UUID cityHallId, Sector sector, String slug, UUID excludedId) {
        boolean exists;
        if (sector == null) {
            exists = excludedId == null
                    ? occupationRepository.existsByCityHall_IdAndSectorIdIsNullAndSlug(cityHallId, slug)
                    : occupationRepository.existsByCityHall_IdAndSectorIdIsNullAndSlugAndIdNot(cityHallId, slug, excludedId);
        } else {
            exists = excludedId == null
                    ? occupationRepository.existsByCityHall_IdAndSectorId_IdAndSlug(cityHallId, sector.getId(), slug)
                    : occupationRepository.existsByCityHall_IdAndSectorId_IdAndSlugAndIdNot(cityHallId, sector.getId(), slug, excludedId);
        }
        if (exists) throw new BusinessException("Ja existe um cargo com este identificador neste setor");
    }

    private String validName(String value) {
        String name = value == null ? "" : value.trim();
        if (name.isBlank()) throw new BusinessException("Informe o nome do cargo");
        if (name.length() > 120) throw new BusinessException("O nome do cargo deve ter no maximo 120 caracteres");
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

    private Employee admin(Employee employee) {
        if (employee == null || employee.getCityHallId() == null)
            throw new UnauthorizedException("O usuario precisa estar vinculado a uma prefeitura");
        if (!Roles.ADMIN.equals(employee.getRole()))
            throw new UnauthorizedException("Somente administradores podem gerenciar cargos");
        return employee;
    }

    private UUID city(Employee employee) {
        if (employee == null || employee.getCityHallId() == null)
            throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee.getCityHallId().getId();
    }
}
