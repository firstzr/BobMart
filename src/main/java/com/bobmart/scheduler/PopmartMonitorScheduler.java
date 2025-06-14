package com.bobmart.scheduler;

import com.bobmart.service.PopmartMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PopmartMonitorScheduler {

    @Autowired
    private PopmartMonitorService monitorService;

    @Scheduled(fixedDelay = 500000)
    public void scheduleMonitoring() {
        log.info("Starting scheduled monitoring cycle");
        monitorService.fullLogic();
    }

} 