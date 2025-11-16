package com.msa.product.domain.tag.service;

import com.msa.product.domain.product.entity.Product;
import com.msa.product.domain.product.repository.ProductRepository;
import com.msa.product.domain.tag.dto.request.RequestTag;
import com.msa.product.domain.tag.dto.response.ResponseTag;
import com.msa.product.domain.tag.entity.ProductTag;
import com.msa.product.domain.tag.entity.Tag;
import com.msa.product.domain.tag.repository.ProductTagRepository;
import com.msa.product.domain.tag.repository.TagRepository;
import com.msa.product.global.exception.CustomException;
import com.msa.product.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductRepository productRepository;

    public ResponseTag getTagById(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        return ResponseTag.builder().name(tag.getName()).build();
    }

    public List<ResponseTag> getTagsByProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        List<Tag> tags = new ArrayList<>();

        for (ProductTag productTag : product.getProductTags()) {
            tags.add(productTag.getTag());
        }

        List<ResponseTag> responseTags = new ArrayList<>();
        tags.forEach(tag -> responseTags.add(ResponseTag.builder().name(tag.getName()).build()));

        return responseTags;
    }

    public void setTag(Long tagId, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        Tag tag = tagRepository.findById(tagId).orElseThrow();

        productTagRepository.save(new ProductTag(product, tag));
    }

    public void updateTag(RequestTag requestTag, Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        tag.updateTag(requestTag);
    }

    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    public void addTag(RequestTag requestTag) {
        Tag tag = Tag.builder().name(requestTag.getName()).build();
        tagRepository.save(tag);
    }
}
