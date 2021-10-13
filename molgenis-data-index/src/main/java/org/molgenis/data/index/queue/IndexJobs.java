package org.molgenis.data.index.queue;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import org.molgenis.data.index.meta.IndexAction;

public class IndexJobs {
  private final ThreadPoolExecutor jobExecutor;
  private final BlockingQueue<Runnable> pendingIndexJobs;

  public IndexJobs(int maxWorkers) {
    pendingIndexJobs = new LinkedBlockingDeque<>();
    jobExecutor = new ThreadPoolExecutor(0, maxWorkers, 10, SECONDS, pendingIndexJobs);
  }

  public void schedule(IndexAction indexAction) {

  }
}
