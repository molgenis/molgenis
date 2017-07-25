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

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
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
			public Job<List<EntityType>> createJob(OneClickImportJobExecution oneClickImportJobExecution)
			{
				final String filename = oneClickImportJobExecution.getFile();
				return (Progress progress) ->
				{
					List<EntityType> entityTypes = oneClickImportJob.getEntityType(progress, filename);
					oneClickImportJobExecution.setEntityTypes(entityTypes);

					String labels = entityTypes.stream()
											.map(entityType -> entityType.getLabel())
											.collect(Collectors.joining(","));

					progress.status(format("Created table(s): %s", labels));
					return entityTypes;
				};
			}
		};
	}
}
