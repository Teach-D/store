package com.example.store.repository;

import com.example.store.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
