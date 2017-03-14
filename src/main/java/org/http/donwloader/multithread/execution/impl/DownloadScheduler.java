package org.http.donwloader.multithread.execution.impl;

import com.google.common.base.Throwables;
import org.http.donwloader.multithread.Download;
import org.http.donwloader.multithread.execution.Scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadScheduler implements Scheduler {
    private static final String RESULT_MESSAGE = "Time spent = %ds, downloaded = %d bytes";
    private final ExecutorService executorService;
    private final MultiThreadDownloader downloader;

    public DownloadScheduler(int numberOfThreads, long maxSpeed) {
        executorService = Executors.newFixedThreadPool(numberOfThreads);
        downloader = new MultiThreadDownloader(maxSpeed);
    }

    @Override
    public String downloadBunch(Collection<Download> toDownload) {
        long start = System.nanoTime();

        //Submitting each download to executor service
        List<Future<Long>> results = new ArrayList<>();
        for (Download download : toDownload) {
            Future<Long> result = executorService.submit(() -> downloader.download(download));
            results.add(result);
        }

        //Waiting for results
        long downloadedTotal = 0;
        for (Future<Long> result : results) {
            try {
                downloadedTotal += result.get();
            } catch (Exception e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }

        long end = System.nanoTime();
        long timeSpentSec = (end - start) / 1000000000;

        return String.format(RESULT_MESSAGE, timeSpentSec, downloadedTotal);
    }

    @Override
    public String downloadSingle(Download toDownload) {
        return downloadBunch(Collections.singleton(toDownload));
    }


    @Override
    public void startDownloader() {
        downloader.start();
    }

    @Override
    public void stopDownloader() {
        downloader.stop();
    }

    @Override
    public String toString() {
        return "DownloadScheduler{" +
                "executorService=" + executorService +
                '}';
    }
}
