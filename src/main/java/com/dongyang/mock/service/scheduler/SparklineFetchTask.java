package com.dongyang.mock.service.scheduler;

import com.dongyang.mock.service.api.fetch.IndexFetchService;

public class SparklineFetchTask implements  Runnable {
    @Override
    public void run() {
        IndexFetchService indexFetchService = new IndexFetchService();
        indexFetchService.runUpdate();
    }
}
