package org.molgenis.file.ingest.execution;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import java.util.function.Function;

@Configuration
@Import(FileIngester.class)
public class FileIngestConfig
{
	@Autowired
	FileIngester fileIngester;

	/**
	 * The FileIngestJob bean factory method.
	 * Prototype scope means that a new bean is generated each time the factory method is called.
	 *
	 * @param jobExecution JobExecution to execute in the {@link FileIngestJob}
	 * @return The {@link FileIngestJob} bean
	 */
	@Bean
	@Scope("prototype")
	public FileIngestJob fileIngestJob(JobExecution jobExecution)
	{
		FileIngestJobExecution fileIngestJobExecution = (FileIngestJobExecution) jobExecution;
		String targetEntityId = fileIngestJobExecution.getTargetEntityId();
		String url = fileIngestJobExecution.getUrl();
		String loader = fileIngestJobExecution.getLoader();

		return new FileIngestJob(fileIngester, targetEntityId, url, loader, fileIngestJobExecution.getIdentifier());
	}

	/**
	 * The FileIngestJob Factory bean.
	 */
	@Bean
	public Function<JobExecution, ? extends Job> fileIngestJobFactory()
	{
		return this::fileIngestJob;
	}
}
