package com.tempo.application.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByName(String name);
    Category findByNameAndUser(String name, User user);
    Optional<Category> findByIdAndUser(int id, User user);
    List<Category> findAllByUserOrderByNameAsc(User user);
}