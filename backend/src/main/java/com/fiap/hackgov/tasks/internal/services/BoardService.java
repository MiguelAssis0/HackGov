package com.fiap.hackgov.tasks.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.services.CityHallService;
import com.fiap.hackgov.cityhall_management.internal.services.SectorService;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tasks.internal.DTOs.Board.CreateBoardDTO;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.mapper.BoardMapper;
import com.fiap.hackgov.tasks.internal.repositories.BoardRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final CityHallService cityHallService;
    private final SectorService sectorService;

    public Board createBoard(CreateBoardDTO createBoardDTO, Employee authenticatedEmployee) {
        Employee currentEmployee = requireAuthenticated(authenticatedEmployee);
        UUID cityHallId = requireCityHallId(currentEmployee);
        Board board = boardMapper.toEntity(createBoardDTO);

        if (board.getCityHall() != null && !cityHallId.equals(board.getCityHall().getId())) {
            throw new BusinessException("O quadro/setor nao pertence a prefeitura do usuario autenticado");
        }

        return boardRepository.save(board);
    }

    public Page<Board> getAllBoards(Pageable pageable, Employee authenticatedEmployee) {
        Employee currentEmployee = requireAuthenticated(authenticatedEmployee);
        return boardRepository.findAllByCityHall_Id(requireCityHallId(currentEmployee), pageable);
    }

    public Board getBoardById(UUID id, Employee authenticatedEmployee) {
        Employee currentEmployee = requireAuthenticated(authenticatedEmployee);
        return boardRepository.findByIdAndCityHall_Id(id, requireCityHallId(currentEmployee))
                .orElseThrow(() -> new ResourceNotFoundException("Quadro/setor nao encontrado para esta prefeitura"));
    }

    private Employee requireAuthenticated(Employee employee) {
        if (employee == null) {
            throw new UnauthorizedException("E necessario estar autenticado para acessar quadros/setores");
        }

        return employee;
    }

    private UUID requireCityHallId(Employee employee) {
        if (employee.getCityHallId() == null) {
            throw new BusinessException("O usuario autenticado precisa estar vinculado a uma prefeitura");
        }

        return employee.getCityHallId().getId();
    }
}
