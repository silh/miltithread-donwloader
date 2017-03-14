package org.http.donwloader.multithread;

import java.io.File;

public class Download {
    private final String url;
    private final String fullFileName;


    public Download(String url, String fileName, String outputDirectory) {
        this.url = url;
        fullFileName = outputDirectory + File.separator + fileName;
    }

    public String getUrl() {
        return url;
    }

    public String getFullFileName() {
        return fullFileName;
    }
}
