package com.dongyang.mock.util;

/**
 * 초기 관리자 계정의 비밀번호 해시를 생성하는 유틸리티
 * 
 * 사용 방법:
 * 1. 이 클래스의 main 메서드를 실행
 * 2. 콘솔에 출력된 INSERT 쿼리를 복사
 * 3. 데이터베이스에서 실행
 */
public class GenerateAdminHash {
    
    public static void main(String[] args) {
        String username = "admin";
        String password = "admin1234";
        String name = "시스템 관리자";
        
        // BCrypt 해시 생성
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        System.out.println("============================================");
        System.out.println("초기 관리자 계정 INSERT 쿼리");
        System.out.println("============================================");
        System.out.println("아이디: " + username);
        System.out.println("비밀번호: " + password + " (해시됨)");
        System.out.println("관리자명: " + name);
        System.out.println("\n생성된 BCrypt 해시:");
        System.out.println(hashedPassword);
        System.out.println("\n============================================");
        System.out.println("실행 가능한 INSERT 쿼리:");
        System.out.println("============================================");
        
        // 실제 INSERT 쿼리 출력
        System.out.println("INSERT INTO admin (admin_username, admin_password, admin_name)");
        System.out.println("VALUES (");
        System.out.println("    '" + username + "',");
        System.out.println("    '" + hashedPassword + "',");
        System.out.println("    '" + name + "'");
        System.out.println(");");
        
        System.out.println("\n============================================");
        System.out.println("주의: 운영 환경에서는 비밀번호를 반드시 변경하세요!");
        System.out.println("============================================");
    }
}

