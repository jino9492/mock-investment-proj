package com.dongyang.mock.model.DTO;

/**
 * API 연동 상태 정보 DTO
 */
public class ApiStatusDTO {
    private boolean isConnected;      // API 연결 상태
    private String status;           // 상태 텍스트 ("정상", "오류", "연결 실패")
    private long responseTime;       // API 응답 시간 (밀리초)
    private String errorMessage;     // 오류 메시지 (있는 경우)

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

