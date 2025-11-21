package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class MemberDTO {
    private int memId; // NOT NULL
    private String memNickName; // NOT NULL
    private String memPassword; // NOT NULL
    private String memName; // NOT NULL
    private Date memBirth; // NOT NULL
    private String memPhone; // NOT NULL
    private String memEmail;

    private Timestamp memJoinAt; // NOT NULL
    private String memLastLoginIP;
    private Timestamp memLastLoginAt;

    private boolean memOpen; // NOT NULL
    private Timestamp agreeTermsAt; // NOT NULL
    private Timestamp agreePrivacyAt; // NOT NULL
    private Timestamp agreeThirdAt;

    private int virtualCash; // NOT NULL

    public int getMemId() {
        return memId;
    }

    public void setMemId(int memId) {
        this.memId = memId;
    }

    public String getMemNickName() {
        return memNickName;
    }

    public void setMemNickName(String memNickName) {
        this.memNickName = memNickName;
    }

    public String getMemPassword() {
        return memPassword;
    }

    public void setMemPassword(String memPassword) {
        this.memPassword = memPassword;
    }

    public String getMemName() {
        return memName;
    }

    public void setMemName(String memName) {
        this.memName = memName;
    }

    public Date getMemBirth() {
        return memBirth;
    }

    public void setMemBirth(Date memBirth) {
        this.memBirth = memBirth;
    }

    public String getMemPhone() {
        return memPhone;
    }

    public void setMemPhone(String memPhone) {
        this.memPhone = memPhone;
    }

    public String getMemEmail() {
        return memEmail;
    }

    public void setMemEmail(String memEmail) {
        this.memEmail = memEmail;
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

    public boolean getMemOpen() {
        return memOpen;
    }

    public void setMemOpen(boolean memOpen) {
        this.memOpen = memOpen;
    }

    public Timestamp getAgreeTermsAt() {
        return agreeTermsAt;
    }

    public void setAgreeTermsAt(Timestamp agreeTermsAt) {
        this.agreeTermsAt = agreeTermsAt;
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

    public int getVirtualCash() {
        return virtualCash;
    }

    public void setVirtualCash(int virtualCash) {
        this.virtualCash = virtualCash;
    }
}
