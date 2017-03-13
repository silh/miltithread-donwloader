package org.http.donwloader.multithread.execution;

import java.util.Collection;

public interface Scheduler {
    String start(Collection<Download> toDownload);
}
