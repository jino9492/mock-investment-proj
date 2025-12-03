# 어드민 페이지 피드백 문서

## 📋 목차
1. [개요](#개요)
2. [구현된 기능](#구현된-기능)
3. [페이지 목록](#페이지-목록)
4. [컨트롤러 구조](#컨트롤러-구조)
5. [데이터베이스 구조](#데이터베이스-구조)
6. [주요 구현 사항](#주요-구현-사항)
7. [개선 사항 및 이슈](#개선-사항-및-이슈)

---

## 개요

이 문서는 MOCK 투자 프로젝트의 어드민 페이지 구현 현황과 피드백을 정리한 문서입니다.

**기본 정보:**
- 어드민 접속 URL: `/admin`
- 로그인 페이지: `/admin` (GET)
- 세션 관리: `adminUser` 속성으로 관리자 정보 저장

---

## 구현된 기능

### ✅ 완료된 기능

1. **관리자 로그인/로그아웃**
   - BCrypt 비밀번호 해싱
   - 세션 기반 인증
   - 로그인 실패 처리
   - 로그아웃 기능

2. **대시보드**
   - 통계 카드 표시
   - 그래프 영역
   - TOP 5 종목 테이블

3. **회원 관리**
   - 회원 목록 조회
   - 회원 상세 정보
   - 필터링 기능

4. **거래 관리**
   - 거래 내역 조회

5. **시스템 설정**
   - API 연동 상태 확인
   - 서버 상태 모니터링

6. **관리자 관리**
   - 관리자 목록 조회
   - 관리자 등록
   - 관리자 정보 수정
   - 관리자 삭제
   - 로그인 ID 중복 체크

---

## 페이지 목록

### 1. 로그인 페이지
- **경로**: `/admin`
- **파일**: `src/main/webapp/WEB-INF/views/admin/login_content.jsp`
- **기능**:
  - 관리자 로그인
  - POST 요청 시에만 오류 메시지 표시
  - 빨간색 오류 메시지

### 2. 대시보드
- **경로**: `/admin/dashboard`
- **파일**: `src/main/webapp/WEB-INF/views/admin/dashboard.jsp`
- **컨트롤러**: `AdminDashboardController.java`
- **기능**:
  - 전체 회원 수 통계
  - 거래 통계
  - 그래프 영역
  - 인기종목 TOP 5

### 3. 회원 관리
- **경로**: `/admin/users`
- **파일**: `src/main/webapp/WEB-INF/views/admin/users.jsp`
- **컨트롤러**: `AdminUsersController.java`
- **기능**:
  - 회원 목록 조회
  - 회원 검색
  - 필터링 (상태, 수익률, 가입기간)
  - 회원 상세 정보 (`/admin/users/{id}`)

### 4. 거래 관리
- **경로**: `/admin/trades`
- **파일**: `src/main/webapp/WEB-INF/views/admin/trades.jsp`
- **컨트롤러**: `AdminTradesController.java`

### 5. 시스템 설정
- **경로**: `/admin/settings`
- **파일**: `src/main/webapp/WEB-INF/views/admin/settings.jsp`
- **컨트롤러**: `AdminSettingsController.java`
- **기능**:
  - API 연동 상태
  - 서버 상태 (CPU, 메모리, 디스크)

### 6. 관리자 관리
- **목록 경로**: `/admin/admins`
- **등록/수정 경로**: `/admin/admins/form`
- **파일**: 
  - `src/main/webapp/WEB-INF/views/admin/admins.jsp` (목록)
  - `src/main/webapp/WEB-INF/views/admin/admin_form.jsp` (등록/수정)
- **컨트롤러**: `AdminAdminsController.java`
- **기능**:
  - 관리자 목록 조회
  - 관리자 등록
  - 관리자 정보 수정
  - 관리자 삭제
  - 로그인 ID 중복 체크

---

## 컨트롤러 구조

### 컨트롤러 파일 위치
```
src/main/java/com/dongyang/mock/controller/admin/
├── AdminController.java          # 로그인/로그아웃 처리
├── BaseAdminController.java      # 공통 기능 베이스 클래스
├── AdminDashboardController.java # 대시보드
├── AdminUsersController.java     # 회원 관리
├── AdminTradesController.java    # 거래 관리
├── AdminSettingsController.java  # 시스템 설정
└── AdminAdminsController.java    # 관리자 관리
```

### 주요 컨트롤러 설명

#### AdminController.java
- **역할**: 로그인/로그아웃 처리
- **경로**: `/admin/*`
- **주요 메서드**:
  - `doGet()`: 로그인 페이지 표시, 로그아웃 처리
  - `doPost()`: 로그인 처리
- **특징**:
  - BCrypt 비밀번호 검증
  - IPv6 localhost를 IPv4로 변환
  - 마지막 로그인 IP/시간 업데이트

#### AdminAdminsController.java
- **역할**: 관리자 관리 (CRUD)
- **경로**: `/admin/admins`, `/admin/admins/form`
- **주요 기능**:
  - 관리자 목록 조회
  - 관리자 등록
  - 관리자 수정
  - 관리자 삭제
  - 로그인 ID 중복 체크

---

## 데이터베이스 구조

### admin 테이블

```sql
CREATE TABLE IF NOT EXISTS admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_username VARCHAR(50) NOT NULL UNIQUE COMMENT '관리자 아이디',
    admin_password VARCHAR(255) NOT NULL COMMENT '해시된 비밀번호',
    admin_name VARCHAR(100) NOT NULL COMMENT '관리자 이름',
    last_login_at TIMESTAMP NULL COMMENT '마지막 로그인 시간',
    last_login_ip VARCHAR(45) NULL COMMENT '마지막 로그인 IP',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_username (admin_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 계정 테이블';
```

### 필드 설명
- `admin_id`: 관리자 고유 ID (자동 증가)
- `admin_username`: 로그인 아이디 (UNIQUE)
- `admin_password`: BCrypt 해시된 비밀번호
- `admin_name`: 관리자 이름
- `last_login_at`: 마지막 로그인 시간
- `last_login_ip`: 마지막 로그인 IP (IPv6 localhost는 자동으로 IPv4로 변환)
- `created_at`: 생성일시
- `updated_at`: 수정일시 (자동 업데이트)

---

## 주요 구현 사항

### 1. 비밀번호 암호화
- **방식**: BCrypt
- **유틸리티**: `PasswordHasher.java`
- **해시 생성 유틸리티**: `GenerateAdminHash.java`

### 2. IP 주소 처리
- IPv6 localhost (`0:0:0:0:0:0:0:1`, `::1`) → IPv4 localhost (`127.0.0.1`)로 자동 변환
- 프록시 환경 대비 `X-Forwarded-For` 헤더 확인
- 표시 시: localhost 변형은 "로컬"로 표시

### 3. 세션 관리
- 로그인 성공 시: `adminUser` 속성에 AdminDTO 저장
- 비밀번호는 세션에 저장하지 않음
- 로그아웃 시: 세션 무효화

### 4. 레이아웃 구조
- **헤더**: `admin_layout_header.jsp`
  - 로고 (왼쪽)
  - 로그아웃 버튼 (오른쪽)
- **사이드바**: 왼쪽 고정 사이드바
  - 대시보드
  - 회원 관리
  - 거래 관리
  - 시스템 설정
  - 관리자 관리

### 5. 공통 기능
- **BaseAdminController**: 어드민 컨트롤러 공통 기능
- **태그 라이브러리**: 모든 어드민 JSP에서 `c:`와 `fmt:` 태그 라이브러리 사용
- **상태 알림**: `admin_status_alert.jsp`로 성공/실패 메시지 표시

---

## 개선 사항 및 이슈

### ✅ 해결된 이슈

1. **IPv6 localhost 표시 문제**
   - 문제: `0:0:0:0:0:0:0:1`로 표시됨
   - 해결: IPv6 → IPv4 변환 및 "로컬" 표시

2. **로그인 오류 메시지 표시**
   - 문제: 새로고침 시에도 오류 메시지 표시
   - 해결: POST 요청일 때만 표시

3. **태그 라이브러리 일관성**
   - 문제: JSP 파일마다 태그 라이브러리 위치가 다름
   - 해결: 모든 어드민 JSP에서 `dashboard.jsp`와 동일한 형식 적용

### 📝 개선 제안

1. **초기 관리자 계정 생성**
   - SQL 파일로 직접 해시된 비밀번호 INSERT
   - 예: `INSERT INTO admin (admin_username, admin_password, admin_name) VALUES ('admin', '$2a$10$...', '관리자');`

2. **권한 관리**
   - 현재는 단순 로그인만 구현
   - 필요 시 역할(Role) 기반 권한 관리 추가 고려

### ⚡ 최적화 사항

1. **보안 및 성능 최적화**
   - 관리자 목록 조회 시 비밀번호 필드 제외 (`getAllAdmins`)
   - 불필요한 데이터 조회 제거로 성능 향상 및 보안 강화

2. **데이터베이스 연결 최적화**
   - 중복 체크와 INSERT/UPDATE를 하나의 트랜잭션으로 통합
   - 불필요한 DB 연결 횟수 감소

3. **쿼리 최적화**
   - 중복 체크 쿼리에 `LIMIT 1` 추가 (최적화)
   - `SELECT 1` 사용으로 불필요한 필드 조회 제거

4. **코드 정리**
   - 사용되지 않는 `getFirstAdmin` 메서드 제거

3. **로그 관리**
   - 관리자 활동 로그 기록 기능
   - 로그인 시도 실패 로그

4. **보안 강화**
   - 로그인 시도 횟수 제한
   - 세션 타임아웃 설정
   - CSRF 토큰 적용

5. **UI/UX 개선**
   - 로딩 상태 표시
   - 폼 유효성 검사 강화
   - 반응형 디자인

---

## 파일 구조

### JSP 파일
```
src/main/webapp/WEB-INF/views/admin/
├── login_content.jsp      # 로그인 페이지
├── dashboard.jsp          # 대시보드
├── users.jsp              # 회원 관리 목록
├── user_detail.jsp        # 회원 상세 정보
├── trades.jsp             # 거래 관리
├── settings.jsp           # 시스템 설정
├── admins.jsp             # 관리자 목록
└── admin_form.jsp         # 관리자 등록/수정 폼
```

### Fragment 파일
```
src/main/webapp/WEB-INF/fragments/admin/
├── admin_layout_head.jsp      # 헤드 섹션
├── admin_layout_header.jsp    # 헤더 (로고, 로그아웃)
├── admin_layout_footer.jsp    # 푸터
└── admin_status_alert.jsp     # 상태 알림

src/main/webapp/WEB-INF/fragments/common/
├── header_admin.jsp           # 어드민 헤더
└── sidebar_left/sidebar.jsp   # 왼쪽 사이드바
```

### DAO/DTO 파일
```
src/main/java/com/dongyang/mock/model/
├── DAO/
│   └── AdminDAO.java          # 관리자 데이터 접근
└── DTO/
    └── AdminDTO.java          # 관리자 데이터 객체
```

---

## 초기 데이터 설정

### 초기 관리자 계정 생성

1. **해시 생성 유틸리티 실행**
   ```java
   // GenerateAdminHash.java 실행
   // 출력된 INSERT 쿼리 복사
   ```

2. **또는 직접 INSERT 쿼리 실행**
   ```sql
   INSERT INTO admin (admin_username, admin_password, admin_name)
   VALUES ('admin', '[BCrypt 해시]', '시스템 관리자');
   ```

3. **기본 계정 정보**
   - 아이디: `admin`
   - 비밀번호: `admin1234`
   - 관리자명: 시스템 관리자

---

## 참고 사항

- 모든 어드민 JSP 파일은 `dashboard.jsp`와 동일한 형식으로 태그 라이브러리를 사용합니다:
  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
  <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
  ```

- 로그인 실패 시 오류 메시지는 POST 요청일 때만 표시됩니다.

- 마지막 로그인 IP는 localhost일 경우 "로컬"로 표시됩니다.

---

**작성일**: 2024년
**최종 수정일**: 최근 업데이트

