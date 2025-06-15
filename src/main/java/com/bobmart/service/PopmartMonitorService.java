package com.bobmart.service;

import com.bobmart.BobmartApplication;
import com.bobmart.model.PopmartSet;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class PopmartMonitorService {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration RESERVATION_TIMEOUT = Duration.ofMinutes(5);
    private static final int MAX_CART_SIZE = 16;

    @Value("${selenium.refresh.interval}")
    private int timer;

    NotificationService notificationService;

    public PopmartMonitorService(NotificationService notificationService) {
        log.info("Initializing BobmartMonitorService");
        this.notificationService = notificationService;

        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        
        // Create a unique user data directory for automation
        String userDataDir = System.getProperty("user.home") + File.separator + "bobmart_chrome_profile";
        options.addArguments("--user-data-dir=" + userDataDir);
        options.addArguments("--profile-directory=Default");
        
        // Basic options
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        
        // Stealth options
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        
        // Set user agent
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36");
        
        // Additional preferences
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, TIMEOUT);
        log.info("BobmartMonitorService initialized successfully");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void monitorSets() throws InterruptedException {
        List<PopmartSet> sets = new ArrayList<>();

        sets.add(new PopmartSet("195"));


        int count =0;
        boolean runFlag= true;
        while (runFlag) {
            for (PopmartSet set : sets){
                try {
                    log.info("starting checking for set {}",set.getName());
                    driver.get(set.getUrl());

                    List<WebElement> buyButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.xpath("//button[contains(., 'Buy Multiple Boxes')]")));

                    log.info("[IMPORTANT]: Set {} is currently open. Attemping Count(Max 10) is {} ",set.getName(), count);
                    notificationService.sendStockNotification(set.getName(),count);
                    sets=new ArrayList<>(List.of(set));
                    count++;
                    if (addToCart(set)) {
                        log.info("Successfully added {} to cart", set.getName());
                    }else {
                        log.info("[IMPORTANT]: Attemping {} FAILED adding {} to cart", set.getName());
                    }
                    if (count>10) runFlag=false;
                } catch (NoSuchWindowException e){
                    log.error("Chrome Window Killed, FETA ERROR, app stopped.");
                   System.exit(1);
                }catch (Exception e){
                    log.info("Stock NotYet Refilled for Set {}",set.getName());
                }
            }
        }
        log.info( "[IMPORTANT]: have tried 10 times to add to cart, now process to reservation time reset logic");
        refreshReservationTimer();
    }

    private boolean addToCart(PopmartSet set) {
        try {

            WebElement boxNumber = driver.findElement(By.xpath("//div[contains(@class, 'index_boxNumber')]"));
            String content = boxNumber.getText();

            WebElement buyButton = driver.findElement(
                By.xpath("//button[contains(., 'Buy Multiple Boxes')]"));
            buyButton.click();

            addGap(1000);
            WebElement checkbox = driver.findElement(By.xpath("//label[contains(@class, 'index_selectAll')]//input[@type='checkbox']"));
            checkbox.click();
            addGap(1000);

            WebElement priceElement = driver.findElement(By.xpath("//div[contains(@class, 'index_price')]"));

            WebElement addToCartButton = driver.findElement(
                        By.xpath("//button[contains(., 'ADD TO BAG')]"));
            addToCartButton.click();
            addGap(1000);
            log.info("add box num {} of set {} for {} dollars", content, set.getName(), priceElement.getText());

            return true;
        } catch (Exception e) {
            log.error("Error adding {} to cart: {}", set.getName(), e.getMessage());
            return false;
        }
    }
    public void refreshReservationTimer() throws InterruptedException {
        int count =0 ;
        log.info("Starting reservation timer refresh loop");
        addGap(10000);
        while (true) {
            try {
                log.info("try refreshReservationTimer, count is {} ",count);
                driver.get("https://www.popmart.com/us/largeShoppingCart");

                WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(text(), 'POP NOW')]")));
                addGap(2000);
                tab = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(text(), 'POP NOW')]")));
                String numbericCartSize = tab.getText().replaceAll("[^0-9.]", "");
                log.info("Cart size is currently read as : {} ",numbericCartSize);
                tab.click();
                addGap(2000);
                if (Integer.parseInt(numbericCartSize)>0){

                    WebElement checkbox = driver.findElement(By.xpath("//div[contains(text(), 'Select all')]"));
                    checkbox.click();
                    addGap(1000);

                    WebElement checkOutButton = driver.findElement(
                            By.xpath("//button[contains(., 'Confirm and Check out')]"));
                    checkOutButton.click();

                    addGap(5000);
                }else {
                    log.error("Cart Size read as 0, current count value(should be 0 as well) is :{} ", count);
                    log.error("[IMPORTANT]: GG! This is a BIG FAIL, GOT NOTHING YOU MOTHER FCKER!!");
                    System.exit(1);
                }
                driver.get("https://www.popmart.com/us/largeShoppingCart");

                log.info("Refresh Succ, current reservation time is : {} ");

            } catch (Exception e) {
                log.error("Error refreshing reservation timer: {}", e.getMessage());

            }
            addGap(120000);
            count++;
        }
    }
    private void addGap(int timer) throws InterruptedException {
        Thread.sleep(timer);
    }
}