package com.dongyang.mock.util.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DBManager {
    private static final Properties props = new Properties();
    private static HikariDataSource dataSource;

    // db 설정 파일 열기 및 Connection Pool 초기화
    static {
        try (InputStream is = DBManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("db.properties 파일을 찾을 수 없습니다.");
            }
            props.load(is);
            
            // HikariCP 설정
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(props.getProperty("db.driver"));
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            
            // Connection Pool 설정
            config.setMinimumIdle(5);                    // 최소 유휴 연결 수
            config.setMaximumPoolSize(20);               // 최대 연결 수
            config.setConnectionTimeout(30000);          // 연결 대기 시간 (30초)
            config.setIdleTimeout(600000);                // 유휴 연결 타임아웃 (10분)
            config.setMaxLifetime(1800000);              // 연결 최대 수명 (30분)
            config.setLeakDetectionThreshold(60000);    // 연결 누수 감지 (60초)
            
            // Connection Pool 초기화
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Connection Pool에서 연결 획득
     * @return Connection Pool에서 가져온 데이터베이스 연결
     * @throws SQLException 연결 획득 실패 시
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * 애플리케이션 종료 시 Connection Pool 정리
     * - HikariCP의 모든 연결을 안전하게 종료
     * - 백그라운드 스레드 정리
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                // HikariCP Connection Pool 종료
                // 이 메서드는 모든 활성 연결을 닫고 풀의 모든 스레드를 종료합니다
                dataSource.close();
                System.out.println("[DBManager] HikariCP Connection Pool이 성공적으로 종료되었습니다.");
            } catch (NoClassDefFoundError e) {
                // 클래스 로더가 이미 파괴된 경우 발생하는 오류는 무시
                // (웹 애플리케이션 종료 시 흔히 발생하는 정상적인 상황)
                System.err.println("[DBManager] Connection Pool 종료 중 클래스 로더가 이미 파괴되었습니다. (정상적인 종료 과정)");
            } catch (Exception e) {
                // 종료 중 예외가 발생해도 로그만 남기고 계속 진행
                // (이미 컨텍스트가 파괴되는 중일 수 있음)
                System.err.println("[DBManager] Connection Pool 종료 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            } catch (Error e) {
                // 기타 Error도 처리 (NoClassDefFoundError 외의 Error)
                System.err.println("[DBManager] Connection Pool 종료 중 심각한 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * DB 자원 해제
     * Connection Pool 사용 시 conn.close()는 실제로 연결을 닫는 것이 아니라
     * Connection Pool로 반환하는 것입니다.
     */
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close(); // Connection Pool로 반환
        } catch (SQLException e) {
            System.err.println("DB 자원 해제 중 오류 발생: " + e.getMessage());
        }
    }

    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }

    /**
     * 단순 DB 연결 헬퍼 메서드
     * - 연결 획득 → 작업 수행 → 연결 해제
     * - 트랜잭션 없이 단순 조회 작업에 사용
     */
    public static <T> T withConnection(SqlFunction<T> function) throws SQLException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            return function.apply(conn);
        } finally {
            DBManager.close(conn, null);
        }
    }

    /**
     * 트랜잭션 관리 헬퍼 메서드
     * - 자동 커밋 비활성화
     * - 작업 수행 후 commit/rollback 결정
     * - 예외 발생 시 자동 rollback
     * - finally에서 자동 커밋 복구 및 연결 해제
     */
    public static <T> T executeInTransaction(TransactionCallback<T> callback) throws SQLException {
        Connection conn = null;
        try {
            System.out.println("[DBManager] 트랜잭션 시작");
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);
            System.out.println("[DBManager] AutoCommit 비활성화 완료");
            
            TransactionResult<T> result = callback.doInTransaction(conn);
            System.out.println("[DBManager] 트랜잭션 콜백 실행 완료. commit: " + result.commit());
            
            if (result.commit()) {
                System.out.println("[DBManager] 트랜잭션 커밋 중...");
                conn.commit();
                System.out.println("[DBManager] 트랜잭션 커밋 완료");
            } else {
                System.out.println("[DBManager] 트랜잭션 롤백 중...");
                conn.rollback();
                System.out.println("[DBManager] 트랜잭션 롤백 완료");
            }
            return result.value();
        } catch (SQLException e) {
            System.err.println("[DBManager] 트랜잭션 중 SQLException 발생: " + e.getMessage());
            System.err.println("[DBManager] SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            if (conn != null) {
                try {
                    System.err.println("[DBManager] 예외 발생으로 인한 롤백 시도...");
                    conn.rollback();
                    System.err.println("[DBManager] 롤백 완료");
                } catch (SQLException rollbackEx) {
                    System.err.println("[DBManager] 롤백 중 오류 발생: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("[DBManager] 트랜잭션 중 예상치 못한 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBManager.close(conn, null);
        }
    }

    /**
     * 단순 SQL 작업을 위한 함수형 인터페이스
     */
    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    /**
     * 트랜잭션 내 작업을 위한 콜백 인터페이스
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        TransactionResult<T> doInTransaction(Connection conn) throws Exception;
    }

    /**
     * 트랜잭션 결과를 담는 내부 클래스
     * - commit 여부와 반환 값을 함께 관리
     */
    public static final class TransactionResult<T> {
        private final T value;
        private final boolean commit;

        public TransactionResult(T value, boolean commit) {
            this.value = value;
            this.commit = commit;
        }

        public static <T> TransactionResult<T> commit(T value) {
            return new TransactionResult<>(value, true);
        }

        public static <T> TransactionResult<T> rollback(T value) {
            return new TransactionResult<>(value, false);
        }

        T value() {
            return value;
        }

        boolean commit() {
            return commit;
        }
    }
}