package com.msa.product.domain.category.controller;


import com.msa.product.domain.category.dto.request.RequestCategory;
import com.msa.product.domain.category.dto.response.ResponseCategory;
import com.msa.product.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity addCategory(@Valid @RequestBody RequestCategory requestCategory, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        categoryService.addCategory(requestCategory);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ResponseCategory>> getAllCategories(){
        return ResponseEntity.status(OK).body(categoryService.getAllCategories());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ResponseCategory> getCategoryById(@PathVariable Long categoryId) {
        return ResponseEntity.status(OK).body(categoryService.getCategoryById(categoryId));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity updateCategory(@PathVariable Long categoryId, @Valid @RequestBody RequestCategory editCategoryDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(objectError -> objectError.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Invalid input");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        categoryService.updateCategory(categoryId, editCategoryDto);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
}
