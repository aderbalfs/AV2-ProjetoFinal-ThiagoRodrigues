package com.soundlist.mapper;

import com.soundlist.dto.MusicRequest;
import com.soundlist.dto.MusicResponse;
import com.soundlist.model.Music;
import com.soundlist.model.Playlist;
import org.springframework.stereotype.Component;

/**
 * Mapper responsável por toda conversão entre Music (entidade JPA) e seus DTOs.
 *
 * Implementado como @Component Spring para ser injetável via construtor nos Services.
 * Toda conversão entidade <-> DTO do domínio Music passa exclusivamente por esta classe,
 * garantindo separação de responsabilidades — Services e Controllers nunca convertem
 * objetos manualmente.
 *
 * Ponto de atenção: o campo playlistId (Long no DTO) é mapeado para um objeto
 * Playlist com apenas o id preenchido. O Hibernate trata essa referência como
 * um proxy gerenciado, equivalente a uma FK válida no banco.
 */
@Component
public class MusicMapper {

    /**
     * Converte MusicRequest (DTO de entrada) → Music (entidade JPA).
     *
     * O campo 'id' não é preenchido — ele é gerado pelo banco no INSERT.
     * O campo 'playlist' é montado com apenas o id, pois o Service já
     * validou que esse playlistId existe no banco antes de chamar o mapper.
     */
    public Music toEntity(MusicRequest request) {
        // Monta referência à Playlist usando apenas o id (FK)
        Playlist playlist = new Playlist();
        playlist.setId(request.playlistId());

        Music music = new Music();
        music.setTitle(request.title());
        music.setArtist(request.artist());
        music.setGenre(request.genre());
        music.setDuration(request.duration());
        music.setPlaylist(playlist);
        return music;
    }

    /**
     * Converte Music (entidade JPA) → MusicResponse (DTO de saída).
     *
     * Expõe playlist.id como playlistId em vez do objeto Playlist completo,
     * evitando referências circulares na serialização JSON
     * (Playlist → musics → Music → playlist → ...).
     */
    public MusicResponse toResponse(Music music) {
        Long playlistId = music.getPlaylist() != null ? music.getPlaylist().getId() : null;
        return new MusicResponse(
                music.getId(),
                music.getTitle(),
                music.getArtist(),
                music.getGenre(),
                music.getDuration(),
                playlistId
        );
    }

    /**
     * Atualiza uma entidade Music existente com os dados do MusicRequest.
     *
     * Recebe a entidade já carregada do banco e sobrescreve apenas os campos
     * do request. O id da entidade é preservado. O Hibernate detecta as
     * mudanças (dirty-checking) e emite o UPDATE automaticamente.
     */
    public void updateEntityFromRequest(MusicRequest request, Music music) {
        Playlist playlist = new Playlist();
        playlist.setId(request.playlistId());

        music.setTitle(request.title());
        music.setArtist(request.artist());
        music.setGenre(request.genre());
        music.setDuration(request.duration());
        music.setPlaylist(playlist);
    }
}
