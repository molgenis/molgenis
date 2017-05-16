package org.molgenis.file.ingest.execution;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.model.ScheduledJobType;
import org.molgenis.data.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@Import(FileIngester.class)
public class FileIngestConfig
{
	@Autowired
	FileIngester fileIngester;

	@Autowired
	ScheduledJobTypeFactory scheduledJobTypeFactory;

	@Autowired
	FileIngestJobExecutionMetaData fileIngestJobExecutionMetaData;

	/**
	 * The FileIngestJob Factory bean.
	 */
	@Bean
	public JobFactory<FileIngestJobExecution> fileIngestJobFactory()
	{
		return new JobFactory<FileIngestJobExecution>()
		{
			@Override
			public Job createJob(FileIngestJobExecution fileIngestJobExecution)
			{
				final String targetEntityId = fileIngestJobExecution.getTargetEntityId();
				final String url = fileIngestJobExecution.getUrl();
				final String loader = fileIngestJobExecution.getLoader();
				return progress -> fileIngester
						.ingest(targetEntityId, url, loader, fileIngestJobExecution.getIdentifier(), progress);
			}
		};
	}

	@Lazy
	@Bean
	public ScheduledJobType fileIngestJobType()
	{
		ScheduledJobType result = scheduledJobTypeFactory.create("fileIngest");
		result.setLabel("File ingest");
		result.setDescription("This job downloads a file from a URL and imports it into MOLGENIS.");
		result.setSchema("{\"title\": \"FileIngest Job\",\n \"type\": \"object\",\n \"properties\": {\n"
				+ "\"url\": {\n\"type\": \"string\",\n\"format\": \"uri\",\n"
				+ "\"description\": \"URL to download the file to ingest from\"\n    },\n"
				+ "\"loader\": {\n \"enum\": [ \"CSV\" ]\n },\n \"targetEntityId\": {\n"
				+ "\"type\": \"string\",\n \"description\": \"ID of the entity to import to\"\n"
				+ "}\n  },\n  \"required\": [\n \"url\",\n \"loader\",\n \"targetEntityId\"\n]\n}");
		result.setJobExecutionType(fileIngestJobExecutionMetaData);
		return result;
	}
}
