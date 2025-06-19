package com.example.authbackend.service;

import com.example.authbackend.entity.VerificationCode;
import com.example.authbackend.repository.VerificationCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public void sendVerificationCode(String email){
        String code = generate6DigitCode();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(LocalDateTime.now());
        verificationCodeRepository.save(verificationCode);

        sendEmailWithCode(email, code);
    }

    private String generate6DigitCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    private void sendEmailWithCode(String email, String code){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verification Code");
        message.setText("Your verification code is: " + code + "\n code is valid for 10 minutes.");
        mailSender.send(message);
    }

}
