package org.molgenis.file.ingest.meta;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.file.ingest.FileIngesterJobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Discovers {@link FileIngest} jobs and schedules them using {@link FileIngesterJobScheduler}
 */
@Component
public class FileIngesterJobRegistrar
{
	private final FileIngesterJobScheduler fileIngesterJobScheduler;
	private final DataService dataService;

	@Autowired
	public FileIngesterJobRegistrar(FileIngesterJobScheduler fileIngesterJobScheduler, DataService dataService) {
		this.fileIngesterJobScheduler = requireNonNull(fileIngesterJobScheduler);
		this.dataService = requireNonNull(dataService);
	}

	public void scheduleJobs() {
		dataService.findAll(FileIngestMetaData.ENTITY_NAME, FileIngest.class).forEach(fileIngesterJobScheduler::schedule);
	}
}
