package org.molgenis.data.jobs.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.jobs.model.ScheduledJobFactory;
import org.molgenis.data.jobs.model.ScheduledJobMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, JobExecutionMetaData.class, ScheduledJobMetadata.class,
		ScheduledJobFactory.class })
public class JobTestConfig
{
}
