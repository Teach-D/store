package com.msa.product.domain.tag.controller;

import com.msa.product.domain.tag.dto.request.RequestTag;
import com.msa.product.domain.tag.dto.response.ResponseTag;
import com.msa.product.domain.tag.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseTag> getTag(@PathVariable Long id) {
        return ResponseEntity.status(OK).body(tagService.getTagById(id));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<List<ResponseTag>> getTagsByProduct(@PathVariable Long id) {
        return ResponseEntity.status(OK).body(tagService.getTagsByProduct(id));
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

        tagService.addTag(requestTag);
        return ResponseEntity.status(OK).build();
    }

    @PostMapping("/{productId}/{tagId}")
    public ResponseEntity setTag(@PathVariable Long productId, @PathVariable Long tagId) {
        tagService.setTag(tagId, productId);
        return ResponseEntity.status(OK).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity updateTag(@Valid @RequestBody RequestTag requestTag, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }
        tagService.updateTag(requestTag, id);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
