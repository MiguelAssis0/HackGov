package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.notice.CreateNoticeDTO;
import com.fiap.hackgov.bidding.internal.DTOs.notice.NoticeResponseDTO;
import com.fiap.hackgov.bidding.internal.entities.Notice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoticeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "licitationProcess", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "impugnations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notice toEntity(CreateNoticeDTO dto);

    @Mapping(target = "licitationProcessId", source = "licitationProcess.id")
    @Mapping(target = "licitationProcessNumber", source = "licitationProcess.processNumber")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", expression = "java(entity.getCreatedBy().getFullName())")
    NoticeResponseDTO toDTO(Notice entity);

    List<NoticeResponseDTO> toDTOList(List<Notice> entities);
}
