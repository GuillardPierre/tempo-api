package com.tempo.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tempo.application.model.category.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByName(String name);
    boolean existsByName(String name);
    boolean existsById(int id);
    void deleteById(int id);
}