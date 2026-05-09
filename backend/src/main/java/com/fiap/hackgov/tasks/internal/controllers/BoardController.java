package com.fiap.hackgov.tasks.internal.controllers;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.services.EmployeeService;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.services.TokenService;
import com.fiap.hackgov.tasks.internal.DTOs.Board.CreateBoardDTO;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.services.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/boards")
@AllArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final TokenService tokenService;
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody @Valid CreateBoardDTO createBoardDTO, @AuthenticationPrincipal Employee employee) {
        Board board = boardService.createBoard(createBoardDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(board.getId()).toUri();
        return ResponseEntity.created(location).body(board);
    }

    @GetMapping
    public ResponseEntity<Page<Board>> getAllBoards(@AuthenticationPrincipal Employee employee, Pageable pageable) {
        Page<Board> response = boardService.getAllBoards(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable UUID id, @AuthenticationPrincipal Employee employee) {
        Board board = boardService.getBoardById(id);
        return ResponseEntity.ok(board);
    }
}
