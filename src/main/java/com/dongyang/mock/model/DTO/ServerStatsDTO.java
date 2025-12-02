package com.dongyang.mock.model.DTO;

/**
 * 서버 상태 정보 DTO
 */
public class ServerStatsDTO {
    private double cpuUsage;      // CPU 사용률 (%)
    private double memoryUsage;   // 메모리 사용률 (%)
    private double diskUsage;     // 디스크 사용률 (%)

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(double diskUsage) {
        this.diskUsage = diskUsage;
    }
}

