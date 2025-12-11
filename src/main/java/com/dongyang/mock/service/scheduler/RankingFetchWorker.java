package com.dongyang.mock.service.scheduler;
import com.dongyang.mock.service.api.fetch.RankingFetchService;

public class RankingFetchWorker implements Runnable{
    private final RankingFetchService rankingFetchService = new RankingFetchService();

    @Override
    public void run() {
        System.out.println("--- 랭킹 업데이트 워커 시작 ---");
        rankingFetchService.runUpdate();
        System.out.println("--- 랭킹 업데이트 워커 종료 ---");
    }
}
