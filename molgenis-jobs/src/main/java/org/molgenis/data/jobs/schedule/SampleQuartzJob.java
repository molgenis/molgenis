package org.molgenis.data.jobs.schedule;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.text.MessageFormat.format;

/**
 * Sample Quartz job.
 * WIP.
 * Instead, we should create a specific Job class for MappingService, Script, FileIngest, etc.
 * <p>
 * It prevents concurrent execution of jobs with the same JobKey
 */
@DisallowConcurrentExecution
public class SampleQuartzJob implements Job
{
	private static final Logger LOG = LoggerFactory.getLogger(SampleQuartzJob.class);

	public SampleQuartzJob()
	{
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		LOG.info(format("Run mapping job with params mappingProjectId {0}, targetEntityId {1}, label {2}, "
						+ "packageId {3} and addSourceAttribute {4}", jobDataMap.getString("mappingProjectId"),
				jobDataMap.getString("targetEntityId"), jobDataMap.getString("label"), jobDataMap.getString("package"),
				jobDataMap.getBoolean("addSourceAttribute")));
	}
}
