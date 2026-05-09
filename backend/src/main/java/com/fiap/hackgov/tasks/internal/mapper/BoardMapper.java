package com.fiap.hackgov.tasks.internal.mapper;

import com.fiap.hackgov.tasks.internal.DTOs.Board.CreateBoardDTO;
import com.fiap.hackgov.tasks.internal.entities.Board;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    Board toEntity(CreateBoardDTO createBoardDTO);

    CreateBoardDTO toDTO(Board board);
}
