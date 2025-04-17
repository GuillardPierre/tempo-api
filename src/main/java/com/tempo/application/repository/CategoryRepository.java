package com.tempo.application.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByName(String name);
    Category findByNameAndUser(String name, User user);
    Optional<Category> findByIdAndUser(int id, User user);
    boolean existsByName(String name);
    boolean existsById(int id);
    void deleteById(int id);
}