package org.molgenis.oneclickimporter.config;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.i18n.PropertiesMessageSource;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.jobs.Progress;
import org.molgenis.oneclickimporter.job.OneClickImportJob;
import org.molgenis.oneclickimporter.job.OneClickImportJobExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Configuration
@Import(OneClickImportJob.class)
public class OneClickImporterConfig
{
	public static final String NAMESPACE = "one-click-importer";

	private final OneClickImportJob oneClickImportJob;

	public OneClickImporterConfig(OneClickImportJob oneClickImportJob)
	{
		this.oneClickImportJob = requireNonNull(oneClickImportJob);
	}

	@Bean
	public PropertiesMessageSource oneClickImportMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
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
					oneClickImportJobExecution.setEntityTypes(createJsonResponse(entityTypes));

					String packageId = entityTypes.get(0).getPackage().getId();
					oneClickImportJobExecution.setPackage(packageId);

					String labels = entityTypes.stream()
											   .map(EntityType::getLabel)
											   .collect(Collectors.joining(","));

					progress.status(format("Created table(s): %s", labels));
					return entityTypes;
				};
			}
		};
	}

	private String createJsonResponse(List<EntityType> entityTypes)
	{
		List<String> responseItems = new ArrayList<>();
		entityTypes.forEach(entityType -> responseItems.add(
				format("{\"id\":\"%s\",\"label\":\"%s\"}", entityType.getId(), entityType.getLabel())));
		return format("[%s]", StringUtils.join(responseItems, ","));
	}

}
