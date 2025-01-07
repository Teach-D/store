package com.example.store.service;

import com.example.store.dto.request.RequestCategory;
import com.example.store.dto.response.ResponseCategory;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.SuccessDto;
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
    public ResponseEntity<SuccessDto> addCategory(RequestCategory requestCategory) {
        Category category = Category.builder().name(requestCategory.getName()).build();
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

    public ResponseDto<List<ResponseCategory>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<ResponseCategory> responseCategories = new ArrayList<>();

        categories.forEach(category -> {
            ResponseCategory result = ResponseCategory.builder().id(category.getId()).name(category.getName()).build();
            responseCategories.add(result);
        });

        return ResponseDto.success(responseCategories);
    }

    public ResponseDto<ResponseCategory> getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(NotFoundCartException::new);
        ResponseCategory result = ResponseCategory.builder().id(category.getId()).name(category.getName()).build();
        return ResponseDto.success(result);
    }

    public ResponseDto<ResponseCategory> updateCategory(Long id, RequestCategory editCategoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(NotFoundCategoryException::new);
        category.updateName(editCategoryDto.getName());

        ResponseCategory responseCategory = ResponseCategory.builder().id(category.getId()).name(category.getName()).build();

        return ResponseDto.success(responseCategory);
    }

    public ResponseEntity<SuccessDto> deleteCategory(Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }
}
