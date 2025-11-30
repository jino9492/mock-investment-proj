package com.dongyang.mock.util.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DBManager {
    private static final Properties props = new Properties();

    // db 설정 파일 열기
    static {
        try (InputStream is = DBManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("db.properties 파일을 찾을 수 없습니다.");
            }
            props.load(is);
            Class.forName(props.getProperty("db.driver"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    // 연결
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
    }

    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
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
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);
            TransactionResult<T> result = callback.doInTransaction(conn);
            if (result.commit()) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return result.value();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } catch (Exception e) {
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