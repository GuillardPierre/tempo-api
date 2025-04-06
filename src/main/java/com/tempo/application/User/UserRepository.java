package com.tempo.application.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    User findById(int id);
    void deleteById(int id);
    boolean existsByEmail(String email);
    boolean existsById(int id);
}
