package org.molgenis.semanticmapper.job;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.semanticmapper.service.MappingService;
import org.molgenis.semanticmapper.service.impl.MappingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@Import(MappingServiceImpl.class)
public class MappingJobConfig
{
	private final MappingService mappingService;
	private final ScheduledJobTypeFactory scheduledJobTypeFactory;
	private final MappingJobExecutionMetadata mappingJobExecutionMetadata;
	private final Gson gson;

	@Autowired
	public MappingJobConfig(MappingService mappingService, ScheduledJobTypeFactory scheduledJobTypeFactory,
			MappingJobExecutionMetadata mappingJobExecutionMetadata)
	{
		this.mappingService = requireNonNull(mappingService);
		this.scheduledJobTypeFactory = requireNonNull(scheduledJobTypeFactory);
		this.mappingJobExecutionMetadata = requireNonNull(mappingJobExecutionMetadata);
		this.gson = new Gson();
	}

	@Autowired
	private MenuReaderService menuReaderService;

	/**
	 * The MappingJob Factory bean.
	 */
	@Bean
	public JobFactory<MappingJobExecution> mappingJobFactory()
	{
		return new JobFactory<MappingJobExecution>()
		{
			@Override
			public Job createJob(MappingJobExecution mappingJobExecution)
			{
				final String mappingProjectId = mappingJobExecution.getMappingProjectId();
				final String targetEntityTypeId = mappingJobExecution.getTargetEntityTypeId();
				final String packageId = mappingJobExecution.getPackageId();
				final String label = mappingJobExecution.getLabel();
				final Boolean addSourceAttribute = mappingJobExecution.isAddSourceAttribute();
				final String resultUrl =
						menuReaderService.getMenu().findMenuItemPath(DataExplorerController.ID) + "?entity="
								+ targetEntityTypeId;
				mappingJobExecution.setResultUrl(resultUrl);
				return progress -> mappingService.applyMappings(mappingProjectId, targetEntityTypeId,
						addSourceAttribute, packageId, label, progress);
			}
		};
	}

	@Lazy
	@Bean
	public ScheduledJobType mappingJobType()
	{
		ScheduledJobType result = scheduledJobTypeFactory.create(MappingJobExecutionMetadata.MAPPING_JOB_TYPE);
		result.setLabel("Mapping");
		result.setDescription("This job runs a Mapping Project.");

		result.setSchema(gson.toJson(of("title", "Mapping Job", "type", "object", "properties",
				of("mappingProjectId", of("type", "string", "description", "The ID of the mapping project"),
						"targetEntityTypeId", of("type", "string", "description",
								"The ID of the created EntityType, may be an existing EntityType"),
						"addSourceAttribute", of("type", "boolean", "description",
								"Indicates if a source attribute should be added to the EntityType, ignored when mapping to an existing EntityType"),
						"packageId", of("type", "string", "description",
								"The ID of the target package, ignored when mapping to an existing EntityType"),
						"label", of("type", "string", "description",
								"The label of the target EntityType, ignored when mapping to an existing EntityType")),
				"required", ImmutableList.of("mappingProjectId", "targetEntityTypeId"))));

		result.setJobExecutionType(mappingJobExecutionMetadata);
		return result;
	}
}
