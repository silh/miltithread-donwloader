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
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private final long maxSpeed;
    private final AtomicLong leftForSec;
    private final int bufferSize;
    private boolean working = true;

    public MultiThreadDownloader(long maxSpeed) {
        this.maxSpeed = maxSpeed;
        this.leftForSec = new AtomicLong(maxSpeed);
        bufferSize = maxSpeed > DEFAULT_BUFFER_SIZE ? DEFAULT_BUFFER_SIZE : (int) maxSpeed;
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
        gate();
        while ((n = in.read(buf)) > 0) {
            for (OutputStream out : outs) {
                out.write(buf, 0, n);
            }
            downloaded += n;
            gate();
        }

        in.close();
        for (OutputStream out : outs) {
            out.close();
        }

        return downloaded;
    }

    private void gate() throws InterruptedException {
        long currentLeft;
        synchronized (leftForSec) {
            do {
                currentLeft = leftForSec.get();
                if (currentLeft < 0) {
                    System.out.println(Thread.currentThread() + "Waiting: current left = " + currentLeft);
                    leftForSec.wait();
                } else {
                    leftForSec.addAndGet(-bufferSize);
                }
            } while (currentLeft < 0);
        }
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
