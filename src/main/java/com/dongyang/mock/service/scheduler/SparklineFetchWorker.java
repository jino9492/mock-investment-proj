package com.dongyang.mock.service.scheduler;

import com.dongyang.mock.service.api.fetch.IndexFetchService;

public class SparklineFetchWorker implements  Runnable {
    @Override
    public synchronized void run() {
        IndexFetchService indexFetchService = new IndexFetchService();
        indexFetchService.runUpdate();
    }
}
