package org.http.donwloader.multithread.execution;

import org.http.donwloader.multithread.execution.impl.MultiThreadDownloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;

public class Download implements Callable<Integer> {
    private final String uri;
    private final Collection<String> fileNames;
    private final String outputDirectory;
    private final MultiThreadDownloader downloader;


    public Download(String uri, Collection<String> fileNames, String outputDirectory, MultiThreadDownloader downloader) {
        this.uri = uri;
        this.fileNames = fileNames;
        this.outputDirectory = outputDirectory;
        this.downloader = downloader;
    }

    @Override
    public Integer call() throws Exception {
//        System.out.println("Starting download of " + uri);
        URL url = new URL(uri);
        ByteArrayOutputStream download = downloader.download(url);

//        System.out.println("Saving results to file(s) " + fileNames);
        for (String file : fileNames) {
            download.writeTo(new FileOutputStream(outputDirectory + File.separator + file));
        }

        return download.size();
    }
}
