package org.molgenis.jobs.config;

import static org.mockito.Mockito.mock;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.jobs.JobExecutionUpdater;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.molgenis.jobs.model.ScheduledJobFactory;
import org.molgenis.jobs.model.ScheduledJobMetadata;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.jobs.model.ScheduledJobTypeMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  JobExecutionMetaData.class,
  ScheduledJobMetadata.class,
  ScheduledJobTypeMetadata.class,
  ScheduledJobTypeFactory.class,
  ScheduledJobFactory.class,
  JobPackage.class
})
public class JobTestConfig {
  @Bean
  public JobExecutionUpdater jobExecutionUpdater() {
    return mock(JobExecutionUpdater.class);
  }
}
