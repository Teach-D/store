package com.example.store.service;

import com.example.store.dto.AddCategoryDto;
import com.example.store.dto.ResponseDto;
import com.example.store.dto.SuccessDto;
import com.example.store.entity.Category;
import com.example.store.exception.ex.NotFoundCartException;
import com.example.store.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public ResponseEntity<SuccessDto> addCategory(AddCategoryDto addCategoryDto) {
        Category category = Category.builder().name(addCategoryDto.getName()).build();
        categoryRepository.save(category);
        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

    public ResponseDto<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseDto.success(categories);
    }

    public ResponseDto<Category> getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(NotFoundCartException::new);
        return ResponseDto.success(category);
    }
}
