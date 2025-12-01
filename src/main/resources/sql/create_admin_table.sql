-- 관리자 테이블 생성
CREATE TABLE IF NOT EXISTS admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_username VARCHAR(50) NOT NULL UNIQUE COMMENT '관리자 아이디',
    admin_password VARCHAR(255) NOT NULL COMMENT '해시된 비밀번호',
    admin_name VARCHAR(100) NOT NULL COMMENT '관리자 이름',
    admin_role ENUM('SUPER_ADMIN', 'ADMIN', 'OPERATOR') NOT NULL DEFAULT 'ADMIN' COMMENT '관리자 역할',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 상태',
    last_login_at TIMESTAMP NULL COMMENT '마지막 로그인 시간',
    last_login_ip VARCHAR(45) NULL COMMENT '마지막 로그인 IP',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_username (admin_username),
    INDEX idx_role (admin_role),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 계정 테이블';

