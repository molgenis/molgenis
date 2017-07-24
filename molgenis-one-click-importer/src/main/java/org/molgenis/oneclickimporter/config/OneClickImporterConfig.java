package org.molgenis.oneclickimporter.config;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.oneclickimporter.job.OneClickImportJob;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static java.util.Objects.requireNonNull;

@Configuration
@Import(OneClickImportJob.class)
public class OneClickImporterConfig
{
	private final OneClickImportJob oneClickImportJob;

	public OneClickImporterConfig(OneClickImportJob oneClickImportJob)
	{
		this.oneClickImportJob = requireNonNull(oneClickImportJob);
	}

	@Bean
	public JobFactory<OneClickImportJobExecution> oneClickImportJobFactory()
	{
		return new JobFactory<OneClickImportJobExecution>()
		{
			@Override
			public Job<EntityType> createJob(OneClickImportJobExecution oneClickImportJobExecution)
			{
				final String filename = oneClickImportJobExecution.getFile();
				return (Progress progress) ->
				{
					EntityType entityType = oneClickImportJob.getEntityType(progress, filename);
					oneClickImportJobExecution.setEntityTypeId(entityType.getId());
					return entityType;
				};
			}
		};
	}
}
