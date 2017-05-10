package org.molgenis.file.ingest.execution;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.model.JobType;
import org.molgenis.data.jobs.model.JobTypeFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.model.FileMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FileIngester.class)
public class FileIngestConfig
{
	@Autowired
	FileIngester fileIngester;

	@Autowired
	JobTypeFactory jobTypeFactory;

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
			public Job<FileMeta> createJob(FileIngestJobExecution fileIngestJobExecution)
			{
				final String targetEntityId = fileIngestJobExecution.getTargetEntityId();
				final String url = fileIngestJobExecution.getUrl();
				final String loader = fileIngestJobExecution.getLoader();
				return progress -> fileIngester
						.ingest(targetEntityId, url, loader, fileIngestJobExecution.getIdentifier(), progress);
			}

			@Override
			public JobType getJobType()
			{
				JobType result = jobTypeFactory.create("fileIngest");
				result.setLabel("File ingest");
				result.setDescription("This job downloads a file from a URL and imports it into MOLGENIS.");
				result.setSchema("{\"title\": \"Mapping Job\",\n \"type\": \"object\",\n \"properties\": {\n"
						+ "\"url\": {\n\"type\": \"string\",\n\"format\": \"uri\",\n"
						+ "\"description\": \"URL to download the file to ingest from\"\n    },\n"
						+ "\"loader\": {\n \"enum\": [ \"CSV\" ]\n },\n \"targetEntityId\": {\n"
						+ "\"type\": \"string\",\n \"description\": \"ID of the entity to import to\"\n"
						+ "}\n  },\n  \"required\": [\n \"url\",\n \"loader\",\n \"targetEntityId\"\n]\n}");
				result.setJobExecutionType(fileIngestJobExecutionMetaData);
				return result;
			}
		};
	}

}
