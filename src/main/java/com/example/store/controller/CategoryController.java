package com.example.store.controller;

import com.example.store.dto.request.RequestCategory;
import com.example.store.dto.response.ResponseCategory;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
import com.example.store.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SuccessDto> addCategory(@RequestBody RequestCategory requestCategory) {
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
    public ResponseEntity<SuccessDto> updateCategory(@PathVariable Long id, @RequestBody RequestCategory editCategoryDto) {
        return categoryService.updateCategory(id, editCategoryDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
