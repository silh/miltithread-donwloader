package org.http.donwloader.multithread.execution.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

public class MultiThreadDownloader {
    private final byte[] maxSpeed;

    public MultiThreadDownloader(int maxSpeed) {
        this.maxSpeed = new byte[maxSpeed];
    }

    public ByteArrayOutputStream download(URL from) throws IOException {

        BufferedInputStream in = new BufferedInputStream(from.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int count;
        while ((count = in.read(maxSpeed, 0, maxSpeed.length)) != -1) {
//            Instant before = Instant.now();
            out.write(maxSpeed, 0, count);
//            Instant after = Instant.now();
//            Duration between = Duration.between(before, after);
//            System.out.println("Time for download " + between.getNano());
        }

        return out;
    }
}
