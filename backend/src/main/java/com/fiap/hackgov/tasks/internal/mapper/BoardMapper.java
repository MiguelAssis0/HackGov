package com.fiap.hackgov.tasks.internal.mapper;

import com.fiap.hackgov.tasks.internal.DTOs.Board.CreateBoardDTO;
import com.fiap.hackgov.tasks.internal.entities.Board;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Board toEntity(CreateBoardDTO createBoardDTO);

    CreateBoardDTO toDTO(Board board);
}
