package com.example.authbackend.service;

import com.example.authbackend.entity.User;
import com.example.authbackend.repository.UserRepository;
import com.example.authbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User register(User user) {
        String hasedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hasedPassword);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email); // repository nên trả về Optional<User>
    }

    public String login(String email, String password){
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElse(null);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtil.generateToken(user.getEmail());
            }
        }
        return null;
    }




}
