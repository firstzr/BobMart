package com.bobmart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.awt.TrayIcon.MessageType;

@Slf4j
@Service
public class NotificationService {
    private final TrayIcon trayIcon;
    private final JavaMailSender mailSender;
    private final String emailRecipient;

    public NotificationService(JavaMailSender mailSender,
                             @Value("${spring.mail.username}") String emailRecipient) {
        this.mailSender = mailSender;
        this.emailRecipient = emailRecipient;
        
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/icon.png"));
                trayIcon = new TrayIcon(image, "Popmart Monitor");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
            } catch (AWTException e) {
                log.error("Failed to initialize system tray", e);
                throw new RuntimeException("Failed to initialize system tray", e);
            }
        } else {
            trayIcon = null;
        }
    }

    public void sendNotification(String title, String message) {
        log.info("Notification: {} - {}", title, message);
        
        // Send desktop notification
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, MessageType.INFO);
        }

        // Send email notification
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(emailRecipient);
            email.setSubject(title);
            email.setText(message);
            mailSender.send(email);
            log.info("Email notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
        }
    }

    public void sendCartFullNotification(int currentSize) {
        sendNotification(
            "Cart Full",
            String.format("Cart has reached the limit of %d items. Monitoring stopped.", currentSize)
        );
    }

    public void sendStockNotification(String setName) {
        sendNotification(
            "Stock Available",
            String.format("Set %s is now in stock!", setName)
        );
    }

    public void sendLoginNotification(boolean success) {
        sendNotification(
            "Login Status",
            success ? "Successfully logged in to Popmart" : "Failed to log in to Popmart"
        );
    }
} 