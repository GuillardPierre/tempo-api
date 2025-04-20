package com.tempo.application.controller;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("all")
    public ResponseEntity<?> getCategories() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        Optional<List<Category>> categories = categoryService.findAllByUserId(user.getId());
        return ResponseEntity.ok(categories);
    }

    @PostMapping("create")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        Category category1 = new Category();
        category1.setName(category.getName());
        category1.setUser(user);

        return ResponseEntity.ok(categoryService.save(category1));
    }
}
