package org.molgenis.data.mapper.job;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.model.ScheduledJobType;
import org.molgenis.data.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.mapper.service.impl.MappingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@Import(MappingServiceImpl.class)
public class MappingJobConfig
{
	@Autowired
	private MappingService mappingService;

	@Autowired
	private ScheduledJobTypeFactory scheduledJobTypeFactory;

	@Autowired
	private MappingJobExecutionMetadata mappingJobExecutionMetadata;

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
				return progress -> mappingService
						.applyMappings(mappingProjectId, targetEntityTypeId, addSourceAttribute, packageId, label,
								progress);
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
		result.setSchema("{\n" + "\"title\": \"Mapping Job\",\n" + "\"type\": \"object\",\n" + "\"properties\": {\n"
				+ "\"mappingProjectId\": {\n" + "\"type\": \"string\",\n"
				+ "\"description\": \"The ID of the mapping project\"\n" + "},\n" + "\"targetEntityTypeId\": {\n"
				+ "\"type\": \"string\",\n"
				+ "\"description\": \"The ID of the created EntityType, may be an existing EntityType\"\n" + "},\n"
				+ "\"addSourceAttribute\": {\n" + "\"type\": \"boolean\",\n"
				+ "\"description\": \"Indicates if a source attribute should be added to the EntityType, ignored when mapping to an existing EntityType\"\n"
				+ "},\n" + "\"packageId\": {\n" + "\"type\": \"string\",\n"
				+ "\"description\": \"The ID of the target package, ignored when mapping to an existing EntityType\"\n"
				+ "},\n" + "\"label\": {\n" + "\"type\": \"string\",\n"
				+ "\"description\": \"The label of the target EntityType, ignored when mapping to an existing EntityType\"\n"
				+ "}\n" + "},\n" + "\"required\": [\"mappingProjectId\", \"targetEntityTypeId\"]\n" + "}");
		result.setJobExecutionType(mappingJobExecutionMetadata);
		return result;
	}
}
