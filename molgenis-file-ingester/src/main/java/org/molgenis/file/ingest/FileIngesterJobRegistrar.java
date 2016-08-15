package org.molgenis.file.ingest;

import org.molgenis.data.DataService;
import org.molgenis.file.ingest.meta.FileIngest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.FILE_INGEST;

/**
 * Discovers {@link FileIngest} jobs and schedules them using {@link FileIngesterJobScheduler}
 */
@Component
public class FileIngesterJobRegistrar
{
	private final FileIngesterJobScheduler fileIngesterJobScheduler;
	private final DataService dataService;

	@Autowired
	public FileIngesterJobRegistrar(FileIngesterJobScheduler fileIngesterJobScheduler, DataService dataService)
	{
		this.fileIngesterJobScheduler = requireNonNull(fileIngesterJobScheduler);
		this.dataService = requireNonNull(dataService);
	}

	public void scheduleJobs()
	{
		dataService.findAll(FILE_INGEST, FileIngest.class).forEach(fileIngesterJobScheduler::schedule);
	}
}
