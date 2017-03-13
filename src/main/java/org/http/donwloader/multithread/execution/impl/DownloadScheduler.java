package org.http.donwloader.multithread.execution.impl;

import com.google.common.base.Throwables;
import org.http.donwloader.multithread.execution.Download;
import org.http.donwloader.multithread.execution.Scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadScheduler implements Scheduler {
    private final ExecutorService executorService;
    private final String resultMessage = "Time spent = %d ms, downloaded = %d bytes";
    private final MultiThreadDownloader downloader;

    public DownloadScheduler(int numberOfThreads, long maxSpeed) {
        executorService = Executors.newFixedThreadPool(numberOfThreads);
        downloader = new MultiThreadDownloader(maxSpeed);
    }

    @Override
    public String start(Collection<Download> toDownload) {
        try {
            long start = System.nanoTime();

            List<Future<Long>> results = new ArrayList<>();
            for (Download download : toDownload) {
                Future<Long> result = executorService.submit(() -> downloader.download(download));
                results.add(result);
            }

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
            long timeSpentSec = (end - start) / 1000000;

            return String.format(resultMessage, timeSpentSec, downloadedTotal);
        } finally {
            downloader.stop();
        }

    }

    @Override
    public String toString() {
        return "DownloadScheduler{" +
                "executorService=" + executorService +
                '}';
    }

}
