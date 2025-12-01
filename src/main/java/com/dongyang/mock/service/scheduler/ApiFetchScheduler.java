package com.dongyang.mock.service.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApiFetchScheduler implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // 스케줄러 생성 (단일 스레드)
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // 5분(300초)마다 실행하도록 예약
        scheduler.scheduleAtFixedRate(
                new SparklineFetchTask(),
                0, // 초기 지연 시간 (0초 후 바로 시작)
                5, // 반복 주기 (5)
                TimeUnit.MINUTES // 시간 단위 (분)
        );

        System.out.println("Scheduler started. Running MyTask every 5 minutes.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            // 3. Tomcat 종료 시 스케줄러 안전하게 종료
            scheduler.shutdownNow();
            System.out.println("Scheduler stopped.");
        }
    }
}