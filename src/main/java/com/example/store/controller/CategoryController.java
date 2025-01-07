package com.example.store.controller;

import com.example.store.dto.request.RequestCategory;
import com.example.store.dto.response.ResponseCategory;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity addCategory(@Valid @RequestBody RequestCategory requestCategory, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(objectError -> {
                String message = objectError.getDefaultMessage();
                sb.append("message :" + message);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        return categoryService.addCategory(requestCategory);
    }

    @GetMapping
    public ResponseDto<List<ResponseCategory>> getAllCategories(){
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public ResponseDto<ResponseCategory> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PutMapping("/{id}")
    public ResponseDto updateCategory(@PathVariable Long id, @Valid @RequestBody RequestCategory editCategoryDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(objectError -> objectError.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Invalid input");

            return ResponseDto.error(HttpStatus.BAD_REQUEST, errorMessage);
        }

        return categoryService.updateCategory(id, editCategoryDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
