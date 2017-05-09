package org.molgenis.file.ingest.execution;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.model.FileMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.function.Function;

@Configuration
@Import(FileIngester.class)
public class FileIngestConfig
{
	@Autowired
	FileIngester fileIngester;

	/**
	 * The FileIngestJob Factory bean.
	 */
	@Bean
	public Function<JobExecution, ? extends Job> fileIngestJobFactory()
	{
		return jobExecution ->
		{
			final FileIngestJobExecution fileIngestJobExecution = (FileIngestJobExecution) jobExecution;
			final String targetEntityId = fileIngestJobExecution.getTargetEntityId();
			final String url = fileIngestJobExecution.getUrl();
			final String loader = fileIngestJobExecution.getLoader();

			return new Job<FileMeta>()
			{
				@Override
				public FileMeta call(Progress progress) throws Exception
				{
					return fileIngester
							.ingest(targetEntityId, url, loader, fileIngestJobExecution.getIdentifier(), progress);
				}

				@Override
				public boolean isTransactional()
				{
					return true;
				}
			};
		};
	}
}
