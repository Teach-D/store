package com.example.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDateTime;
import java.util.Date;

import static org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Board {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;
    private String content;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime createDate;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime updateDate;

    public void updateBoard(Board board) {
        this.title = board.getTitle();
        this.content = board.getContent();
        this.updateDate = board.getUpdateDate();
    }
}
