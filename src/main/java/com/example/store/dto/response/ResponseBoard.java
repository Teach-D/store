package com.example.store.dto.response;

import com.example.store.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBoard {

    private String title;
    private String content;

    public Page<ResponseBoard> toDtoPage(Page<Board> boardPage) {
        return boardPage.map(m -> ResponseBoard.builder()
                .title(m.getTitle())
                .content(m.getContent())
                .build());
    }
}
