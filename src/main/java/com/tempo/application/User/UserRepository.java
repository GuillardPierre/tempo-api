package com.tempo.application.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    User findByEmail(String email);
    User findById(int id);
    void deleteById(int id);
    boolean existsByEmail(String email);
    boolean existsById(int id);
}
