package com.dongyang.mock.model.DTO;

public class RealtimeDTO {
    private String curTime; // 체결시간
    private String curPrice; // 현재가
    private String changePrice; // 전일 대비
    private String fluRt; // 등락률

    public RealtimeDTO(String curTime, String curPrice, String changePrice, String fluRt) {
        this.curTime = curTime;
        this.curPrice = curPrice;
        this.changePrice = changePrice;
        this.fluRt = fluRt;
    }

    public String getCurTime() {
        return curTime;
    }

    public void setCurTime(String curTime) {
        this.curTime = curTime;
    }

    public String getCurPrice() {
        return curPrice;
    }

    public void setCurPrice(String curPrice) {
        this.curPrice = curPrice;
    }

    public String getChangePrice() {
        return changePrice;
    }

    public void setChangePrice(String changePrice) {
        this.changePrice = changePrice;
    }

    public String getFluRt() {
        return fluRt;
    }

    public void setFluRt(String fluRt) {
        this.fluRt = fluRt;
    }
}
