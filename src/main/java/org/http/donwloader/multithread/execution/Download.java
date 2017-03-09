package org.http.donwloader.multithread.execution;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

public class Download {
    private final String url;
    private final Collection<String> fullFileNames;
    private final String outputDirectory;


    public Download(String url, Collection<String> fileNames, String outputDirectory) {
        this.url = url;
        this.fullFileNames = fileNames.stream()
                .map(fileName -> (outputDirectory + File.separator + fileName))
                .collect(Collectors.toList());
        this.outputDirectory = outputDirectory;
    }

    public String getUrl() {
        return url;
    }

    public Collection<String> getFullFileNames() {
        return fullFileNames;
    }
}
