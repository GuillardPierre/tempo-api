package com.tempo.application.controller;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

        List<Category> categories = categoryService.findAllByUserId(user.getId());
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable int id, @RequestBody Category categoryUpdate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        // Récupérer directement la catégorie par id et utilisateur
        Category category = categoryService.findByIdAndUser(id, user);
        if (category == null) {
            return ResponseEntity.status(403).body("Accès refusé : vous n'êtes pas propriétaire de cette catégorie");
        }
        category.setName(categoryUpdate.getName());
        return ResponseEntity.ok(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        // Récupérer directement la catégorie par id et utilisateur
        Category category = categoryService.findByIdAndUser(id, user);
        if (category == null) {
            return ResponseEntity.status(403).body("Accès refusé : vous n'êtes pas propriétaire de cette catégorie");
        }
        categoryService.deleteCategory(category);
        return ResponseEntity.ok().build();
    }
}
