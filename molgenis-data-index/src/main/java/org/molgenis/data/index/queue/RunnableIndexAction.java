package org.molgenis.data.index.queue;

import org.molgenis.data.index.job.IndexJobService;
import org.molgenis.data.index.meta.IndexAction;

public class RunnableIndexAction implements Runnable{

  private IndexAction indexAction;
  private IndexJobService indexJobService;

  public RunnableIndexAction(IndexAction indexAction, IndexJobService indexJobService) {

  }

  @Override
  public void run() {
    // voer de action aan de service, log nog maar even naar de console
  }
}
