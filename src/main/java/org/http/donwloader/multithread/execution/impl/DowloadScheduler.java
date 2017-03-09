package org.http.donwloader.multithread.execution.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import org.http.donwloader.multithread.execution.Download;
import org.http.donwloader.multithread.execution.Scheduler;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DowloadScheduler implements Scheduler {
    private final ExecutorService executorService;
    private final Multimap<String, String> urisAndFiles;
    private final Path outputDirectory;
    private final String resultMessage = "Time spent = %d s, downloaded = %d bytes";
    private final MultiThreadDownloader downloader;

    public DowloadScheduler(int numberOfThreads,
                            long maxSpeed,
                            Multimap<String, String> urisAndFiles,
                            Path outputDirectory) {
        executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.urisAndFiles = urisAndFiles;
        this.outputDirectory = outputDirectory;
        downloader = new MultiThreadDownloader(maxSpeed);
    }

    @Override
    public String start() {
        Instant timeStarted = Instant.now();

        List<Future<Long>> results = new ArrayList<>();
        for (String uri : urisAndFiles.keySet()) {
            Collection<String> files = urisAndFiles.get(uri);
            Download download = new Download(uri, files, outputDirectory.toString());
            Future<Long> result =
                    executorService.submit(() -> downloader.download(download));
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

        downloader.stop();

        Instant timeEnded = Instant.now();
        Duration between = Duration.between(timeStarted, timeEnded);
        long seconds = between.getSeconds();

        return String.format(resultMessage, seconds, downloadedTotal);
    }

    @Override
    public String toString() {
        return "DowloadScheduler{" +
                "executorService=" + executorService +
                ", urisAndFiles=" + urisAndFiles +
                ", outputDirectory=" + outputDirectory +
                '}';
    }

}
