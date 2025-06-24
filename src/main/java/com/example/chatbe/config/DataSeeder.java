package com.example.chatbe.config;

import com.example.chatbe.entity.User;
import com.example.chatbe.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // ✅ Inject encoder

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            User user1 = new User();
            user1.setFullName("User A");
            user1.setEmail("a@example.com");
            user1.setPassword(passwordEncoder.encode("pass")); // ✅ Hash
            user1.setAvatarUrl("/images/defaulter.png");

            User user2 = new User();
            user2.setFullName("User B");
            user2.setEmail("b@example.com");
            user2.setPassword(passwordEncoder.encode("pass"));
            user2.setAvatarUrl("/images/defaulter.png");

            User user3 = new User();
            user3.setFullName("User C");
            user3.setEmail("c@example.com");
            user3.setPassword(passwordEncoder.encode("pass"));
            user3.setAvatarUrl("/images/defaulter.png");

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);

            System.out.println("✅ Đã tạo user test với mật khẩu đã mã hóa");
        }
    }
}
