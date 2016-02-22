package org.molgenis.file.ingest;

import org.molgenis.data.Entity;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FileIngester quartz job
 */
public class FileIngesterJob implements Job
{
	public static final String ENTITY_KEY = "entity";

	@Autowired
	FileIngester fileIngester;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		Entity entity = (Entity) context.getMergedJobDataMap().get(ENTITY_KEY);
		RunAsSystemProxy.runAsSystem(() -> fileIngester.ingest(entity));
	}
}
