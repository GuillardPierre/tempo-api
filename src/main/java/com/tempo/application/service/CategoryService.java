package com.tempo.application.service;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.CategoryRepository;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.repository.WorktimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WorktimeRepository worktimeRepository;

    public List<Category> findAllByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("USER" + user );
        return categoryRepository.findAllByUserOrderByNameAsc(user);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Category category) {
        worktimeRepository.deleteByCategory(category);
        categoryRepository.delete(category);
    }

    public Category findByIdAndUser(int id, User user) {
        return categoryRepository.findByIdAndUser(id, user).orElse(null);
    }
}
