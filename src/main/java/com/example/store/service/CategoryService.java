package com.example.store.service;

import com.example.store.dto.AddCategoryDto;
import com.example.store.dto.ResponseCategoryDto;
import com.example.store.dto.ResponseDto;
import com.example.store.dto.SuccessDto;
import com.example.store.entity.Category;
import com.example.store.exception.ex.NotFoundCartException;
import com.example.store.exception.ex.NotFoundCategoryException;
import com.example.store.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public ResponseDto<List<ResponseCategoryDto>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<ResponseCategoryDto> responseCategories = new ArrayList<>();

        categories.forEach(category -> {
            ResponseCategoryDto result = ResponseCategoryDto.builder().id(category.getId()).name(category.getName()).build();
            responseCategories.add(result);
        });

        return ResponseDto.success(responseCategories);
    }

    public ResponseDto<ResponseCategoryDto> getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(NotFoundCartException::new);
        ResponseCategoryDto result = ResponseCategoryDto.builder().id(category.getId()).name(category.getName()).build();
        return ResponseDto.success(result);
    }

    public ResponseEntity<SuccessDto> updateCategory(Long id, AddCategoryDto editCategoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(NotFoundCategoryException::new);
        category.updateName(editCategoryDto.getName());
        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity<SuccessDto> deleteCategory(Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }
}
