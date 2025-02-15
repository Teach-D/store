package com.example.store.service;

import com.example.store.dto.request.RequestBoard;
import com.example.store.dto.response.ResponseBoard;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Board;
import com.example.store.entity.Member;
import com.example.store.repository.BoardRepository;
import com.example.store.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public ResponseDto<ResponseBoard> getBoard(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        boolean writtenBy = isWrittenBy(board, memberId);
        ResponseBoard responseBoard = null;

        if (writtenBy) {
            responseBoard = ResponseBoard.builder().title(board.getTitle()).content(board.getContent()).build();
        } else {
            throw new RuntimeException();
        }

        return ResponseDto.success(responseBoard);
    }

    public ResponseDto<Page<ResponseBoard>> getAllBoard(int page) {
        int size = 10;
        Page<Board> boards = boardRepository.findAll(PageRequest.of(page, size));
        Page<ResponseBoard> responseBoards = new ResponseBoard().toDtoPage(boards);

        return ResponseDto.success(responseBoards);
    }

    public ResponseEntity<SuccessDto> createBoard(Long memberId, RequestBoard requestBoard) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Board board = Board.builder().title(requestBoard.getTitle()).content(requestBoard.getContent()).member(member).createDate(LocalDateTime.now()).build();
        boolean writtenBy = isWrittenBy(board, memberId);

        if (writtenBy) {
            boardRepository.save(board);
        } else {
            throw new RuntimeException();
        }

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> updateBoard(Long memberId, Long beforeBoardId, RequestBoard afterBoard) {
        Board board = Board.builder().title(afterBoard.getTitle()).content(afterBoard.getContent()).updateDate(LocalDateTime.now()).build();
        log.info(LocalDateTime.now().toString());
        Board beforeBoard = boardRepository.findById(beforeBoardId).orElseThrow();
        boolean writtenBy = isWrittenBy(beforeBoard, memberId);

        if (writtenBy) {
            beforeBoard.updateBoard(board);
        } else {
            throw new RuntimeException();
        }


        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteBoard(Long memberId, Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        boolean writtenBy = isWrittenBy(board, memberId);

        if (writtenBy) {
            boardRepository.deleteById(boardId);
        } else {
            throw new RuntimeException();
        }

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public boolean isWrittenBy(Board board, Long memberId) {
        if (board.getMember().getId().equals(memberId)) {
            return true;
        } else {
            return false;
        }
    }

}
