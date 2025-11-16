package com.msa.product.domain.category.service;

import com.msa.product.domain.category.dto.request.RequestCategory;
import com.msa.product.domain.category.dto.response.ResponseCategory;
import com.msa.product.domain.category.entity.Category;
import com.msa.product.domain.category.repository.CategoryRepository;
import com.msa.product.global.exception.CustomException;
import com.msa.product.global.exception.ErrorCode;
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
    public void addCategory(RequestCategory requestCategory) {
        Category category = Category.builder().name(requestCategory.getName()).build();
        categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

    public List<ResponseCategory> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<ResponseCategory> responseCategories = new ArrayList<>();

        categories.forEach(category -> {
            ResponseCategory result = ResponseCategory.builder().id(category.getId()).name(category.getName()).build();
            responseCategories.add(result);
        });
        return responseCategories;
    }

    public ResponseCategory getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        return ResponseCategory.builder().id(category.getId()).name(category.getName()).build();
    }

    public ResponseCategory updateCategory(Long categoryId, RequestCategory editCategoryDto) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        category.updateName(editCategoryDto.getName());

        return ResponseCategory.builder().id(category.getId()).name(category.getName()).build();
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
