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

        // 5분마다 실행: 주가 데이터 가져오기
        scheduler.scheduleAtFixedRate(
                new SparklineFetchTask(),
                0, // 초기 지연 시간 (0초 후 바로 시작)
                5, // 반복 주기 (5분)
                TimeUnit.MINUTES // 시간 단위 (분)
        );

        System.out.println("Scheduler started. Running SparklineFetchTask every 5 minutes.");

        // 1분마다 실행: 조건 주문 체크 및 자동 주문 생성
        scheduler.scheduleAtFixedRate(
                new ConditionalOrderCheckTask(),
                10, // 초기 지연 시간 (10초 후 시작)
                1, // 반복 주기 (1분)
                TimeUnit.MINUTES // 시간 단위 (분)
        );

        System.out.println("Scheduler started. Running ConditionalOrderCheckTask every 1 minute.");

        // 30초마다 실행: PENDING 주문 체결 처리
        scheduler.scheduleAtFixedRate(
                new OrderExecutionTask(),
                20, // 초기 지연 시간 (20초 후 시작)
                30, // 반복 주기 (30초)
                TimeUnit.SECONDS // 시간 단위 (초)
        );

        System.out.println("Scheduler started. Running OrderExecutionTask every 30 seconds.");
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