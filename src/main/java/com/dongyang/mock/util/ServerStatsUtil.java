package com.dongyang.mock.util;

import com.dongyang.mock.model.DTO.ServerStatsDTO;
import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * 서버 상태 정보를 가져오는 유틸리티 클래스
 */
public class ServerStatsUtil {

    /**
     * 서버 상태 정보 조회
     * @return 서버 상태 정보 (CPU, 메모리, 디스크 사용률)
     */
    public static ServerStatsDTO getServerStats() {
        ServerStatsDTO stats = new ServerStatsDTO();
        
        try {
            // CPU 사용률
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double cpuUsage = osBean.getProcessCpuLoad() * 100.0;
            if (cpuUsage < 0) {
                cpuUsage = 0.0;
            }
            stats.setCpuUsage(cpuUsage);

            // 메모리 사용률
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            long usedMemory = heapUsage.getUsed();
            long maxMemory = heapUsage.getMax();
            double memoryUsage = (maxMemory > 0) ? (usedMemory * 100.0 / maxMemory) : 0.0;
            stats.setMemoryUsage(memoryUsage);

            // 디스크 사용률 (애플리케이션이 실행되는 디스크)
            File root = new File("/");
            if (!root.exists()) {
                // Windows 환경
                File[] roots = File.listRoots();
                if (roots != null && roots.length > 0) {
                    root = roots[0];
                }
            }
            
            if (root.exists()) {
                long totalSpace = root.getTotalSpace();
                long freeSpace = root.getFreeSpace();
                long usedSpace = totalSpace - freeSpace;
                double diskUsage = (totalSpace > 0) ? (usedSpace * 100.0 / totalSpace) : 0.0;
                stats.setDiskUsage(diskUsage);
            } else {
                stats.setDiskUsage(0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 발생 시 기본값 설정
            stats.setCpuUsage(0.0);
            stats.setMemoryUsage(0.0);
            stats.setDiskUsage(0.0);
        }

        return stats;
    }
}

