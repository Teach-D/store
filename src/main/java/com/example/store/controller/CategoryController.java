package com.example.store.controller;

import com.example.store.dto.AddCategoryDto;
import com.example.store.dto.ResponseCategoryDto;
import com.example.store.dto.ResponseDto;
import com.example.store.dto.SuccessDto;
import com.example.store.entity.Category;
import com.example.store.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<SuccessDto> addCategory(@RequestBody AddCategoryDto addCategoryDto) {
        return categoryService.addCategory(addCategoryDto);
    }

    @GetMapping
    public ResponseDto<List<ResponseCategoryDto>> getAllCategories(){
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public ResponseDto<ResponseCategoryDto> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessDto> updateCategory(@PathVariable Long id, @RequestBody AddCategoryDto editCategoryDto) {
        return categoryService.updateCategory(id, editCategoryDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
