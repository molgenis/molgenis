package org.molgenis.navigator.delete.job;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.navigator.delete.ResourceDeleteService;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.springframework.stereotype.Component;

@Component
class ResourceDeleteJobExecutionJobFactory extends JobFactory<ResourceDeleteJobExecution> {

  private final ResourceDeleteService resourceDeleteService;

  ResourceDeleteJobExecutionJobFactory(ResourceDeleteService resourceDeleteService) {
    this.resourceDeleteService = requireNonNull(resourceDeleteService);
  }

  @Override
  public Job<Void> createJob(ResourceDeleteJobExecution jobExecution) {
    final List<ResourceIdentifier> resources = jobExecution.getResources();
    return progress -> resourceDeleteService.delete(resources, progress);
  }
}
