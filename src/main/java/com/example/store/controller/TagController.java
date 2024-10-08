package com.example.store.controller;

import com.example.store.dto.request.RequestTag;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseTag;
import com.example.store.service.MemberService;
import com.example.store.service.ProductService;
import com.example.store.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;
    private final ProductService productService;
    private final MemberService memberService;

    @GetMapping("/{id}")
    public ResponseDto<ResponseTag> getTag(@PathVariable Long id) {
        return tagService.getTagById(id);
    }

    @GetMapping("/products/{id}")
    public ResponseDto<List<ResponseTag>> getTagsByProduct(@PathVariable Long id) {
        return tagService.getTagsByProduct(id);
    }

    @PostMapping
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity addTag(@Valid @RequestBody RequestTag requestTag, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        return tagService.addTag(requestTag);
    }

    @PostMapping("/{productId}/{tagId}")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity setTag(@PathVariable Long productId, @PathVariable Long tagId) {
        return tagService.setTag(tagId, productId);
    }

    @PatchMapping("/{id}")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity updateTag(@Valid @RequestBody RequestTag requestTag, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        return tagService.updateTag(requestTag, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTag(@PathVariable Long id) {
        return tagService.deleteTag(id);
    }
}
