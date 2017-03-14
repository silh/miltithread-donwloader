package org.http.donwloader.multithread.execution;

import org.http.donwloader.multithread.Download;

import java.util.Collection;

public interface Scheduler {
    /**
     * Download a collection of files into specified locations.
     *
     * @param toDownload - collection of links and output files.
     * @return resulting message in format Time spent = %ds, downloaded = %d bytes
     */
    String downloadBunch(Collection<Download> toDownload);

    /**
     * Download a file into specified location
     *
     * @param toDownload - link and output file
     * @return resulting message in format Time spent = %ds, downloaded = %d bytes
     */
    String downloadSingle(Download toDownload);

    /**
     * Starts the downloader inner speed limiter. If turned off - number of bytes available to download for each
     * second won't be updated and download won't be possible.
     */
    void startDownloader();

    /**
     * Stops the downloader inner speed limiter. If turned off - number of bytes available to download for each
     * second won't be updated and download won't be possible.
     */
    void stopDownloader();
}
