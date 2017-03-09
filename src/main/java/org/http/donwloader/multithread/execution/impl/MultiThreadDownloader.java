package org.http.donwloader.multithread.execution.impl;

import org.http.donwloader.multithread.execution.Download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MultiThreadDownloader {
    private static final int DEFAULT_BUFFER_ZISE = 8192;
    private final long maxSpeed;
    private final AtomicLong leftForSec;
    private final int bufferSize;
    private boolean working = true;

    public MultiThreadDownloader(long maxSpeed) {
        this.maxSpeed = maxSpeed;
        this.leftForSec = new AtomicLong(maxSpeed);
        bufferSize = maxSpeed > DEFAULT_BUFFER_ZISE ? DEFAULT_BUFFER_ZISE : (int) maxSpeed;
        new Thread(() -> {
            while (working) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                synchronized (leftForSec) {
                    leftForSec.set(this.maxSpeed);
                    leftForSec.notifyAll();
                }
            }
        }).start();
    }

    public long download(Download download) throws IOException, InterruptedException {
        String urlString = download.getUrl();
        URL url = new URL(urlString);
        Collection<String> fullFileNames = download.getFullFileNames();
        InputStream in = url.openStream();
        List<OutputStream> outs = new ArrayList<>();
        for (String fullFileName : fullFileNames) {
            outs.add(new FileOutputStream(fullFileName));
        }

        long downloaded = 0L;
        byte[] buf = new byte[bufferSize];
        int n;
        long currentLeft;
        synchronized (leftForSec) {
            currentLeft = leftForSec.get();
            if (currentLeft < bufferSize) {
                leftForSec.wait();
            }
        }
        while ((n = in.read(buf)) > 0) {
            leftForSec.addAndGet(-n);
            for (OutputStream out : outs) {
                out.write(buf, 0, n);
            }
            downloaded += n;

            synchronized (leftForSec) {
                currentLeft = leftForSec.get();
                if (currentLeft < bufferSize) {
                    System.out.println("Waiting: current left = " + currentLeft);
                    leftForSec.wait();
                }
            }
        }

        return downloaded;
    }

    public synchronized void start() {
        if (!working) {
            new Thread(() -> {
                while (working) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    synchronized (leftForSec) {
                        leftForSec.set(maxSpeed);
                        leftForSec.notifyAll();
                    }
                }
            }).start();
            working = true;
        }
    }

    public synchronized void stop() {
        if (working) {
            working = false;
        }
    }
}
