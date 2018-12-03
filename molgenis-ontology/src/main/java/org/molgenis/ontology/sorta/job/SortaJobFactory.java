package org.molgenis.ontology.sorta.job;

import static java.util.Objects.requireNonNull;

import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SortaJobFactory {
  private final SortaJobProcessor sortaJobProcessor;

  public SortaJobFactory(SortaJobProcessor sortaJobProcessor) {
    this.sortaJobProcessor = requireNonNull(sortaJobProcessor);
  }

  @Bean
  public JobFactory<SortaJobExecution> sortaJobExecutionJobFactory() {
    return new JobFactory<SortaJobExecution>() {

      @Override
      public Job createJob(SortaJobExecution jobExecution) {
        return progress ->
            sortaJobProcessor.process(
                jobExecution.getOntologyIri(),
                jobExecution.getSourceEntityName(),
                jobExecution.getResultEntityName(),
                progress);
      }
    };
  }
}
