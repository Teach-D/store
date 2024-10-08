package com.example.store.service;

import com.example.store.dto.request.RequestTag;
import com.example.store.dto.response.ResponseDto;
import com.example.store.dto.response.ResponseTag;
import com.example.store.dto.response.SuccessDto;
import com.example.store.entity.Product;
import com.example.store.entity.ProductTag;
import com.example.store.entity.Tag;
import com.example.store.repository.MemberRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.ProductTagRepository;
import com.example.store.repository.TagRepository;
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

    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductRepository productRepository;

    public ResponseDto<ResponseTag> getTagById(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        ResponseTag responseTag = ResponseTag.builder().name(tag.getName()).build();

        return ResponseDto.success(responseTag);
    }

    public ResponseDto<List<ResponseTag>> getTagsByProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        List<Tag> tags = new ArrayList<>();

        for (ProductTag productTag : product.getProductTags()) {
            tags.add(productTag.getTag());
        }

        List<ResponseTag> responseTags = new ArrayList<>();

        tags.forEach(tag -> responseTags.add(ResponseTag.builder().name(tag.getName()).build()));

        return ResponseDto.success(responseTags);
    }

    public ResponseEntity setTag(Long tagId, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        Tag tag = tagRepository.findById(tagId).orElseThrow();

        productTagRepository.save(new ProductTag(product, tag));

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity updateTag(RequestTag requestTag, Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        tag.updateTag(requestTag);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity deleteTag(Long id) {
        tagRepository.deleteById(id);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }

    public ResponseEntity addTag(RequestTag requestTag) {
        Tag tag = Tag.builder().name(requestTag.getName()).build();
        tagRepository.save(tag);

        return ResponseEntity.ok().body(SuccessDto.valueOf("true"));
    }
}
