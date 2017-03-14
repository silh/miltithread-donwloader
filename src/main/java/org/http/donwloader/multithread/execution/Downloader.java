package org.http.donwloader.multithread.execution;

import org.http.donwloader.multithread.Download;

public interface Downloader {
    /**
     * Downloads a file to specified location
     *
     * @param download address to download from and to
     * @return number of bytes downloaded
     */
    long download(Download download);

    /**
     * Starts the downloader inner speed limiter. If turned off - number of bytes available to download for each
     * second won't be updated and download won't be possible.
     */
    void start();

    /**
     * Stops the downloader inner speed limiter. If turned off - number of bytes available to download for each
     * second won't be updated and download won't be possible.
     */
    void stop();
}
