package org.molgenis.data.jobs.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, JobExecutionMetaData.class, })
public class JobTestConfig
{
}
