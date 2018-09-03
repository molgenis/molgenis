package org.molgenis.jobs;

import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.jobs.model.JobExecution;
import org.springframework.stereotype.Component;

/** Gets the JobFactory for a JobExecution. */
@Component
public class JobFactoryRegistry {
  private final Map<Class<?>, JobFactory> jobFactories;

  public JobFactoryRegistry() {
    jobFactories = new HashMap<>();
  }

  public void registerJobFactory(JobFactory jobFactory) {
    jobFactories.put(resolveTypeArgument(jobFactory.getClass(), JobFactory.class), jobFactory);
  }

  public JobFactory getJobFactory(JobExecution jobExecution) {
    return jobFactories.get(jobExecution.getClass());
  }
}
