package com.example.store.controller;

import com.example.store.dto.request.RequestBoard;
import com.example.store.dto.response.ResponseBoard;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Member;
import com.example.store.jwt.util.IfLogin;
import com.example.store.jwt.util.LoginUserDto;
import com.example.store.service.BoardService;
import com.example.store.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        return boardService.getBoard(boardId, member.getId());
    }

    @PostMapping
    public ResponseEntity createBoard(@IfLogin LoginUserDto loginUserDto, @Valid @RequestBody RequestBoard requestBoard, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return boardService.createBoard(member.getId(), requestBoard);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity updateBoard(@IfLogin LoginUserDto loginUserDto, @PathVariable Long boardId, @Valid @RequestBody RequestBoard requestBoard, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return boardService.updateBoard(member.getId(), boardId, requestBoard);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<SuccessDto> deleteBoard(@IfLogin LoginUserDto loginUserDto, @PathVariable Long boardId) {
        Member member = memberService.findByEmail(loginUserDto.getEmail());

        return boardService.deleteBoard(member.getId(), boardId);
    }
}
