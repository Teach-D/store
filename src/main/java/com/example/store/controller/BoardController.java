package com.example.store.controller;

import com.example.store.dto.request.EditProductDto;
import com.example.store.dto.request.RequestBoard;
import com.example.store.dto.response.ResponseBoard;
import com.example.store.dto.response.ResponseCartItem;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Board;
import com.example.store.entity.Member;
import com.example.store.entity.Product;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.BoardService;
import com.example.store.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MemberService memberService;

    @GetMapping
    public ResponseDto<Page<ResponseBoard>> getAllBoards(@RequestParam(required = false, defaultValue = "0") int page) {
        return boardService.getAllBoard(page);
    }

    @GetMapping("/{boardId}")
    public ResponseDto<ResponseBoard> getBoardById(@IfLogin LoginUserDto loginUserDto,  @PathVariable Long boardId) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());
        return boardService.getBoard(boardId, member.getMemberId());
    }

    @PostMapping
    public ResponseEntity<SuccessDto> createBoard(@IfLogin LoginUserDto loginUserDto, @RequestBody RequestBoard requestBoard) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return boardService.createBoard(member.getMemberId(), requestBoard);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<SuccessDto> updateBoard(@IfLogin LoginUserDto loginUserDto, @RequestBody RequestBoard requestBoard, @PathVariable Long boardId) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return boardService.updateBoard(member.getMemberId(), boardId, requestBoard);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<SuccessDto> deleteBoard(@IfLogin LoginUserDto loginUserDto, @PathVariable Long boardId) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return boardService.deleteBoard(member.getMemberId(), boardId);
    }
}
