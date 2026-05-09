package com.fiap.hackgov.tasks.internal.services;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.services.CityHallService;
import com.fiap.hackgov.cityhall_management.internal.services.SectorService;
import com.fiap.hackgov.shared.infra.services.TokenService;
import com.fiap.hackgov.tasks.internal.DTOs.Board.CreateBoardDTO;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.mapper.BoardMapper;
import com.fiap.hackgov.tasks.internal.repositories.BoardRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final CityHallService cityHallService;
    private final SectorService sectorService;

    public Board createBoard(CreateBoardDTO createBoardDTO){
        Board board = boardMapper.toEntity(createBoardDTO);
        return boardRepository.save(board);
    }

    public Page<Board> getAllBoards(Pageable pageable) {
        Page<Board> board = boardRepository.findAll(pageable);

        return board;
    }

    public Board getBoardById(UUID id) {
        return boardRepository.findById(id).orElseThrow(() -> new RuntimeException("Board not found"));
    }
}
