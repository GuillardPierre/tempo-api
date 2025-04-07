package com.tempo.application.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tempo.application.model.user.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    User findById(int id);
    void deleteById(int id);
    boolean existsByEmail(String email);
    boolean existsById(int id);
}
