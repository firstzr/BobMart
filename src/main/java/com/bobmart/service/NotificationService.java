package com.bobmart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendNotification(String title, String message) {
        log.info("Notification: {} - {}", title, message);

        List<String> emailRecipients = Arrays.asList("firstzr1@gmail.com", "amyzz7288@gmail.com","sunrenjun1992@hotmail.com","justinmac054@gmail.com");
        for (String emailRecipient : emailRecipients){
            try {
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(emailRecipient);
                email.setSubject(title);
                email.setText(message);
                mailSender.send(email);
            } catch (Exception e) {
                log.error("Failed to send email notification", e);
            }
        }
    }
    @Async
    public void sendStockNotification(String setName, int count) {
        if (count==0){
            sendNotification(
                    String.format("Stock Available for POPMART Set %s", setName),
                    String.format("Set %s is now in stock!", setName)
            );
        }
    }
} 