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
        scheduler = Executors.newScheduledThreadPool(2);

        // 5분(300초)마다 실행하도록 예약
        scheduler.scheduleAtFixedRate(
                new SparklineFetchWorker(),
                0, // 초기 지연 시간 (0초 후 바로 시작)
                5, // 반복 주기 (5)
                TimeUnit.MINUTES // 시간 단위 (분)
        );
        System.out.println("지수 데이터 Scheduler started. Running MyTask every 5 minutes.");

        // 1분(60초)마다 실행하도록 예약
        scheduler.scheduleAtFixedRate(
                new RankingFetchWorker(),
                0, // 초기 지연 시간 (0초 후 바로 시작)
                1, // 반복 주기 (1)
                TimeUnit.MINUTES // 시간 단위 (분)
        );
        System.out.println("랭킹 데이터 Scheduler started. Running every 1 second (API limit).");

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