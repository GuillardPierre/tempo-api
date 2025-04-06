package com.tempo.application.User;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tempo.application.config.JwtUtils;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequestMapping(path="/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public @ResponseBody String signupUser(@RequestBody User request) { 
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email déjà utilisé.";
        }
        return userService.register(request).toString();
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User request) {
        // User user = userRepository.findByEmail(request.getEmail());        
        // if (user != null && encoder.matches(request.getPassword(), user.getPassword())) {
        //     return "Login successful! Welcome, " + user.getFullName() + "!";
        // } else {
        //     return "Invalid email or password.";
        // }
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            if (authentication.isAuthenticated()) {
                 Map<String, Object> authData = new HashMap<>();
                authData.put("token", jwtUtils.generateToken(request.getEmail()));
                authData.put("type", "Bearer");
                return ResponseEntity.ok(authData);
            }
            return ResponseEntity.status(401).body("Invalid email or password.");
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid email or password.");
        }
    }
}
