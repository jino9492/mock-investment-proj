package com.dongyang.mock.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    // 비밀번호 해싱
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    // 해싱된 비밀번호 비교
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}