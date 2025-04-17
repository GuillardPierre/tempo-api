package com.tempo.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tempo.application.model.user.User;
import com.tempo.application.model.user.UserCreateDto;
import com.tempo.application.model.user.UserDto;
import com.tempo.application.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserDto getUserById(int id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return toDto(user);
    }

    private UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    private User toEntity(UserCreateDto userCreateDto){
        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(userCreateDto.getPassword());
        return user;
    }

    public UserDto register(UserCreateDto userCreateDto) {
        if (userRepository.existsByEmail(userCreateDto.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = toEntity(userCreateDto);
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return toDto(user);
    }
}
