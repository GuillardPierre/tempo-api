package com.tempo.application.service;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.CategoryRepository;
import com.tempo.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    public Optional<List<Category>> findAllByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return categoryRepository.findCategoriesByUser(user);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }
}
