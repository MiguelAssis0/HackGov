package com.fiap.hackgov.bidding.internal.mappers;

import com.fiap.hackgov.bidding.internal.DTOs.PageResponseDTO;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.function.Function;

@Mapper(componentModel = "spring")
public interface PageMapper {
    /**
     * Mapeia uma Page<T> para PageResponseDTO<R> usando uma função de conversão.
     *
     * @param page Page com os dados da entidade original
     * @param mapper função para converter T em R (ex: entity -> dto)
     * @param <T> tipo da entidade
     * @param <R> tipo do DTO de resposta
     * @return PageResponseDTO com conteúdo mapeado
     */
    default <T, R> PageResponseDTO<R> toPageResponseDto(Page<T> page, Function<T, R> mapper) {
        return PageResponseDTO.of(page, mapper);
    }
}
