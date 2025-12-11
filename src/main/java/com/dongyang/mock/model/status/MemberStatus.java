package com.dongyang.mock.model.status;

public final class MemberStatus {
    private MemberStatus() {}

    public enum JoinResult {
        SUCCESS,
        DUPLICATE_EMAIL,
        DUPLICATE_NICKNAME,
        REQUIRED_AGREEMENT_MISSING,
        FAIL_INSERT // DB 삽입 실패
    }

    public enum UpdateResult {
        SUCCESS,
        DUPLICATE_EMAIL,
        DUPLICATE_NICKNAME,
        FAIL
    }
}
