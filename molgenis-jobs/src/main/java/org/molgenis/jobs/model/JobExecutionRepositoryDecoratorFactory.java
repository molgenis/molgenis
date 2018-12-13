package org.molgenis.jobs.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.springframework.stereotype.Component;

@Component
public class JobExecutionRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<JobExecution, JobExecutionMetaData> {

  public JobExecutionRepositoryDecoratorFactory(JobExecutionMetaData jobExecutionMetaData) {
    super(jobExecutionMetaData);
  }

  @Override
  public Repository<JobExecution> createDecoratedRepository(Repository<JobExecution> repository) {
    return new JobExecutionRepositoryDecorator(repository);
  }
}
