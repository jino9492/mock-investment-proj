package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class MemberDTO {
    private int memId;
    private String memEmail;
    private String memPassword;
    private String memNickname;

    private Timestamp memJoinAt;
    private String memLastLoginIP;
    private Timestamp memLastLoginAt;

    private Timestamp agreePrivacyAt;
    private Timestamp agreeThirdAt;
    private Timestamp agreeNotificationAt;

    private int virtualCash;

    public int getMemId() {
        return memId;
    }

    public void setMemId(int memId) {
        this.memId = memId;
    }

    public String getMemEmail() {
        return memEmail;
    }

    public void setMemEmail(String memEmail) {
        this.memEmail = memEmail;
    }

    public String getMemPassword() {
        return memPassword;
    }

    public void setMemPassword(String memPassword) {
        this.memPassword = memPassword;
    }

    public String getMemNickname() {
        return memNickname;
    }

    public void setMemNickname(String memNickname) {
        this.memNickname = memNickname;
    }

    public Timestamp getMemJoinAt() {
        return memJoinAt;
    }

    public void setMemJoinAt(Timestamp memJoinAt) {
        this.memJoinAt = memJoinAt;
    }

    public String getMemLastLoginIP() {
        return memLastLoginIP;
    }

    public void setMemLastLoginIP(String memLastLoginIP) {
        this.memLastLoginIP = memLastLoginIP;
    }

    public Timestamp getMemLastLoginAt() {
        return memLastLoginAt;
    }

    public void setMemLastLoginAt(Timestamp memLastLoginAt) {
        this.memLastLoginAt = memLastLoginAt;
    }

    public Timestamp getAgreePrivacyAt() {
        return agreePrivacyAt;
    }

    public void setAgreePrivacyAt(Timestamp agreePrivacyAt) {
        this.agreePrivacyAt = agreePrivacyAt;
    }

    public Timestamp getAgreeThirdAt() {
        return agreeThirdAt;
    }

    public void setAgreeThirdAt(Timestamp agreeThirdAt) {
        this.agreeThirdAt = agreeThirdAt;
    }

    public Timestamp getAgreeNotificationAt() {
        return agreeNotificationAt;
    }

    public void setAgreeNotificationAt(Timestamp agreeNotificationAt) {
        this.agreeNotificationAt = agreeNotificationAt;
    }

    public int getVirtualCash() {
        return virtualCash;
    }

    public void setVirtualCash(int virtualCash) {
        this.virtualCash = virtualCash;
    }
}
