package org.molgenis.gavin.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.index.meta.IndexPackage;
import org.molgenis.gavin.job.meta.GavinJobExecutionFactory;
import org.molgenis.gavin.job.meta.GavinJobExecutionMetaData;
import org.molgenis.jobs.config.JobTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, JobTestConfig.class, GavinJobExecutionMetaData.class,
		GavinJobExecutionFactory.class, IndexPackage.class })
public class GavinTestConfig
{
}
