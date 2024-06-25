package com.example.store.controller;

import com.example.store.dto.AddCategoryDto;
import com.example.store.entity.Category;
import com.example.store.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Category addCategory(@RequestBody AddCategoryDto addCategoryDto) {
        log.info("aa");
        return categoryService.addCategory(addCategoryDto);
    }

    @GetMapping
    public List<Category> getAllCategories(){
        return categoryService.getCategories();
    }
}
