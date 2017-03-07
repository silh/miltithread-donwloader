package org.http.donwloader.multithread.execution.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import org.http.donwloader.multithread.exceptions.AlreadyExecutedException;
import org.http.donwloader.multithread.exceptions.NotStartedException;
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
    private final MultiThreadDownloader downloader;
    private final Multimap<String, String> urisAndFiles;
    private final Path outputDirectory;
    private final List<Future<Integer>> results;
    private final String resultMessage = "Time spent = %d s, downloaded = %d bytes";
    private boolean executed = false;
    private Instant timeStarted;
    private Instant timeEnded;

    public DowloadScheduler(int numberOfThreads,
                            int maxSpeed,
                            Multimap<String, String> urisAndFiles,
                            Path outputDirectory) {
        executorService = Executors.newFixedThreadPool(numberOfThreads);
        downloader = new MultiThreadDownloader(maxSpeed);
        this.urisAndFiles = urisAndFiles;
        this.outputDirectory = outputDirectory;
        results = new ArrayList<>();
    }

    @Override
    public void start() {
        if (!executed) {
            executed = true;
            timeStarted = Instant.now();
            for (String uri : urisAndFiles.keySet()) {
                Collection<String> files = urisAndFiles.get(uri);
                Future<Integer> result =
                        executorService.submit(new Download(uri, files, outputDirectory.toString(), downloader));
                results.add(result);
            }
        } else {
            throw new AlreadyExecutedException();
        }
    }

    @Override
    public String getResults() {
        int downloaded = 0;
        if (executed) {
            for (Future<Integer> result : results) {
                try {
                    downloaded += result.get();
                } catch (Exception e) {
                    Throwables.throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }
            timeEnded = Instant.now();
            Duration between = Duration.between(timeStarted, timeEnded);
            long seconds = between.getSeconds();

            return String.format(resultMessage, seconds, downloaded);
        } else {
            throw new NotStartedException();
        }

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
