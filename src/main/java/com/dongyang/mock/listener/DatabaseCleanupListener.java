package com.dongyang.mock.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * 애플리케이션 종료 시 JDBC 드라이버를 등록 해제하여 메모리 누수를 방지하는 리스너
 */
public class DatabaseCleanupListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(DatabaseCleanupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 애플리케이션 시작 시에는 아무 작업도 하지 않음
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 등록된 모든 JDBC 드라이버를 등록 해제
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                logger.info("JDBC 드라이버 등록 해제: " + driver.getClass().getName());
            } catch (SQLException e) {
                logger.warning("JDBC 드라이버 등록 해제 실패: " + driver.getClass().getName() + " - " + e.getMessage());
            }
        }

        // MySQL의 AbandonedConnectionCleanupThread 종료
        try {
            Class<?> cleanupThreadClass = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            // MySQL Connector/J 8.0 이상에서는 checkedShutdown() 사용
            try {
                java.lang.reflect.Method checkedShutdown = cleanupThreadClass.getMethod("checkedShutdown");
                checkedShutdown.invoke(null);
                logger.info("MySQL AbandonedConnectionCleanupThread 종료됨 (checkedShutdown)");
            } catch (NoSuchMethodException e) {
                // checkedShutdown()이 없으면 shutdown() 시도 (구버전)
                try {
                    java.lang.reflect.Method shutdown = cleanupThreadClass.getMethod("shutdown");
                    shutdown.invoke(null);
                    logger.info("MySQL AbandonedConnectionCleanupThread 종료됨 (shutdown)");
                } catch (NoSuchMethodException ex) {
                    logger.fine("MySQL cleanup thread 종료 메서드를 찾을 수 없음");
                } catch (Exception ex) {
                    logger.warning("MySQL cleanup thread 종료 중 오류: " + ex.getMessage());
                }
            } catch (Exception e) {
                logger.warning("MySQL cleanup thread checkedShutdown 호출 중 오류: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            // MySQL 드라이버가 없는 경우 무시
            logger.fine("MySQL cleanup thread 클래스를 찾을 수 없음");
        } catch (Exception e) {
            // 기타 예외는 무시
            logger.fine("MySQL cleanup thread 종료 시도 중 오류: " + e.getMessage());
        }
    }
}

