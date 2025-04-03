package com.tempo.application.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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

    @PostMapping("/signup")
    public @ResponseBody String signupUser(@RequestBody User request) { 
        return userService.register(request).toString();
    }
    
    @PostMapping("/login")
    public @ResponseBody String loginUser(@RequestBody User request) {
        User user = userRepository.findByEmail(request.getEmail());        
        if (user != null && encoder.matches(request.getPassword(), user.getPassword())) {
            return "Login successful! Welcome, " + user.getFullName() + "!";
        } else {
            return "Invalid email or password.";
        }
    }
}
