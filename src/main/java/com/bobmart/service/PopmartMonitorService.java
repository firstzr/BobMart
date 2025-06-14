package com.bobmart.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PopmartMonitorService {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Duration RESERVATION_TIMEOUT = Duration.ofMinutes(5);
    private static final int MAX_CART_SIZE = 10;

    @Value("${popmart.username}")
    private String username;

    @Value("${popmart.password}")
    private String password;

    public PopmartMonitorService() {
        log.info("Initializing PopmartMonitorService");
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless=new");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36");
        options.setExperimentalOption("useAutomationExtension", false);


        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, TIMEOUT);
        log.info("PopmartMonitorService initialized successfully");
    }

//    public void monitorSets(List<PopmartSet> sets) {
//        try {
//            int currentCartSize = getCartSize();
//            log.info("Current cart size: {}", currentCartSize);
//
//            if (currentCartSize >= MAX_CART_SIZE) {
//                log.info("Cart size limit reached ({}), stopping monitoring", currentCartSize);
//                return;
//            }
//
//            for (PopmartSet set : sets) {
//                if (!set.isMonitored()) {
//                    continue;
//                }
//
//                try {
//                    driver.get(set.getUrl());
//
//                    List<WebElement> buyButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
//                        By.xpath("//button[contains(., 'Buy Multiple Boxes')]")));
//
//                    if (buyButtons.isEmpty()) {
//                        log.info("Set {} is out of stock", set.getName());
//                        continue;
//                    }
//
//                    if (addToCart(set)) {
//                        log.info("Successfully added {} to cart", set.getName());
//                        break;
//                    }
//                } catch (Exception e) {
//                    log.error("Error monitoring set {}: {}", set.getName(), e.getMessage());
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error during monitoring: {}", e.getMessage());
//        }
//    }

    private int getCartSize() {
        try {
            driver.get("https://www.popmart.com/us/cart");
            List<WebElement> cartItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".cart-item")));
            log.info("Found {} items in cart", cartItems.size());
            return cartItems.size();
        } catch (Exception e) {
            log.error("Error getting cart size: {}", e.getMessage());
            return 0;
        }
    }

//    private boolean addToCart(PopmartSet set) {
//        try {
//            log.info("Adding {} to cart", set.getName());
//            WebElement buyButton = wait.until(ExpectedConditions.elementToBeClickable(
//                By.xpath("//button[contains(., 'Buy Multiple Boxes')]")));
//            buyButton.click();
//
//            WebElement quantityInput = wait.until(ExpectedConditions.presenceOfElementLocated(
//                By.cssSelector("input[type='number']")));
//            quantityInput.clear();
//            quantityInput.sendKeys(String.valueOf(set.getTargetQuantity()));
//
//            WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
//                By.xpath("//button[contains(., 'Add to Cart')]")));
//            addToCartButton.click();
//
//            return true;
//        } catch (Exception e) {
//            log.error("Error adding {} to cart: {}", set.getName(), e.getMessage());
//            return false;
//        }
//    }

    public void refreshReservationTimer() {
        try {
            log.info("Starting reservation timer refresh loop");
            while (true) {
                driver.get("https://www.popmart.com/us/cart");
                
                WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'Checkout')]")));
                checkoutButton.click();
                
                Thread.sleep(1000);
                
                driver.get("https://www.popmart.com/us/cart");
                
                Thread.sleep(Duration.ofMinutes(4).toMillis());
            }
        } catch (InterruptedException e) {
            log.info("Reservation timer refresh loop ended");
        } catch (Exception e) {
            log.error("Error refreshing reservation timer: {}", e.getMessage());
        }
    }

    public void close() {
        if (driver != null) {
            driver.quit();
            log.info("Browser closed");
        }
    }

    public void fullLogic() {
        bobmartLogin();
    }

    private boolean bobmartLogin() {
        try {
            log.info("Starting login process");
            driver.get("https://www.popmart.com/us/user/login");

            handlePop();

            // Wait for the email input field
            log.info("Waiting for email input");
            WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[id='email']")));
            log.info("Entering email: {}", username);
            emailInput.sendKeys(username);

            log.info("Looking for login button");
            WebElement loginUserButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'CONTINUE')]")));
            log.info("Clicking login button");
            loginUserButton.click();

            // Wait for the password input field
            log.info("Waiting for password input");
            WebElement passwordInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[id='password']")));
            log.info("Entering password");
            passwordInput.sendKeys(password);

            // Click the login button
            log.info("Looking for login button");
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'SIGN IN')]")));
            log.info("Clicking login button");
            loginButton.click();


            System.exit(1);
            // Wait for login to complete and redirect to account page
            log.info("Waiting for redirect to account page");
            wait.until(ExpectedConditions.urlContains("popmart.com/us/account"));

            log.info("Successfully logged in to Popmart");
            return true;
        } catch (Exception e) {
            log.error("Failed to log in to Popmart", e);
            return false;
        }
    }

    private void handlePop() {
        // Handle cookie consent popup
        try {
            log.info("Looking for cookie consent popup");
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class, 'policy_accept')]")));

            log.info("Cookie consent accepted");
            acceptButton.click();
        } catch (Exception e) {
            log.info("No cookie consent popup found or already accepted");
        }
    }

}