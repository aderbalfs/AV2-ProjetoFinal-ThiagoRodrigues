package com.soundlist.mapper;

import com.soundlist.dto.MusicResponse;
import com.soundlist.dto.PlaylistRequest;
import com.soundlist.dto.PlaylistResponse;
import com.soundlist.model.Playlist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Mapper responsável por toda conversão entre Playlist (entidade JPA) e seus DTOs.
 *
 * Depende do MusicMapper para converter as músicas aninhadas na resposta.
 * A injeção via construtor (@RequiredArgsConstructor do Lombok) garante que
 * o Spring injete o MusicMapper corretamente ao criar este bean.
 *
 * Regra arquitetural: qualquer conversão de Playlist em qualquer camada
 * DEVE passar por este mapper — nunca conversão manual no Service ou Controller.
 */
@Component
@RequiredArgsConstructor
public class PlaylistMapper {

    // Usado para converter as músicas da playlist no toResponse
    private final MusicMapper musicMapper;

    /**
     * Converte PlaylistRequest (DTO de entrada) → Playlist (entidade JPA).
     *
     * O campo 'id' não é preenchido — gerado pelo banco.
     * O campo 'musics' não é preenchido — músicas são gerenciadas
     * separadamente pelo MusicService.
     */
    public Playlist toEntity(PlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.name());
        playlist.setDescription(request.description());
        return playlist;
    }

    /**
     * Converte Playlist (entidade JPA) → PlaylistResponse (DTO de saída).
     *
     * Converte cada Music da lista usando o MusicMapper para garantir
     * que nenhuma entidade JPA vaze para fora desta camada.
     *
     * Se a lista de músicas for nula (playlist recém-criada), retorna lista vazia.
     */
    public PlaylistResponse toResponse(Playlist playlist) {
        List<MusicResponse> musicResponses = playlist.getMusics() != null
                ? playlist.getMusics().stream()
                        .map(musicMapper::toResponse)
                        .toList()
                : Collections.emptyList();

        return new PlaylistResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getDescription(),
                musicResponses
        );
    }

    /**
     * Atualiza uma entidade Playlist existente com os dados do PlaylistRequest.
     *
     * Preserva o id e a lista de músicas — apenas name e description são atualizados.
     * O Hibernate detecta as mudanças e emite o UPDATE automaticamente.
     */
    public void updateEntityFromRequest(PlaylistRequest request, Playlist playlist) {
        playlist.setName(request.name());
        playlist.setDescription(request.description());
    }
}
