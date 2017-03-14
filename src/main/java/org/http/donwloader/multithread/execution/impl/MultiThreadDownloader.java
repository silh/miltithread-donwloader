package org.http.donwloader.multithread.execution.impl;

import org.http.donwloader.multithread.Download;
import org.http.donwloader.multithread.execution.Downloader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MultiThreadDownloader implements Downloader {
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private final long maxSpeed;
    private final int bufferSize;
    private final Object lock = new Object();
    private volatile long leftForSec;
    private boolean working = false;

    public MultiThreadDownloader(long maxSpeed) {
        this.maxSpeed = maxSpeed;
        this.leftForSec = maxSpeed;
        bufferSize = maxSpeed > DEFAULT_BUFFER_SIZE ? DEFAULT_BUFFER_SIZE : (int) maxSpeed;
        start();
    }

    @Override
    public long download(Download download) {
        try {
            String urlString = download.getUrl();
            URL url = new URL(urlString);
            String fullFileName = download.getFullFileName();

            try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(fullFileName)) {
                long downloaded = 0L;
                byte[] buf = new byte[bufferSize];
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    downloaded += n;
                    getSpeed();
                }
                return downloaded;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    This method tries to acquire speed for the thread in a size of buffer size.
     */
    private void getSpeed() throws InterruptedException {
        synchronized (lock) {
            boolean waited = false;
            long start = System.currentTimeMillis();
            while (leftForSec <= 0) {
                waited = true;
                lock.wait();
            }
            long end = System.currentTimeMillis();
            if (waited) {
                System.out.printf("Thread = %s, waited for = %d \n", Thread.currentThread(), (end - start));
            }
            leftForSec = leftForSec - bufferSize;
        }
    }

    @Override
    public synchronized void start() {
        if (!working) {
            new Thread(() -> {
                while (working) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (lock) {
                        leftForSec = maxSpeed;
                        lock.notifyAll();
                    }
                }
            }).start();
            working = true;
        }
    }

    @Override
    public synchronized void stop() {
        if (working) {
            working = false;
        }
    }
}
