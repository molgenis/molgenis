package org.molgenis.file.ingest.execution;

import org.molgenis.core.ui.file.FileDownloadController;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.jobs.Progress;
import org.springframework.stereotype.Component;

import java.io.File;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.DatabaseAction.ADD_UPDATE_EXISTING;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;

/**
 * Imports a file from a remote url.
 * <p>
 * For now only csv files are supported
 */
@Component
public class FileIngester
{
	private final FileStoreDownload fileStoreDownload;
	private final ImportServiceFactory importServiceFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final FileMetaFactory fileMetaFactory;
	private final DataService dataService;

	public FileIngester(FileStoreDownload fileStoreDownload, ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, FileMetaFactory fileMetaFactory,
			DataService dataService)
	{
		this.fileStoreDownload = requireNonNull(fileStoreDownload);
		this.importServiceFactory = requireNonNull(importServiceFactory);
		this.fileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.dataService = requireNonNull(dataService);
	}

	/**
	 * Imports a csv file defined in the fileIngest entity
	 *
	 * @see FileIngestJobExecutionMetaData
	 */
	public FileMeta ingest(String entityTypeId, String url, String loader, String jobExecutionID, Progress progress)
	{
		if (!"CSV".equals(loader))
		{
			throw new FileIngestException("Unknown loader '" + loader + "'");
		}

		progress.setProgressMax(2);
		progress.progress(0, "Downloading url '" + url + "'");
		File file = fileStoreDownload.downloadFile(url, jobExecutionID, entityTypeId + ".csv");
		progress.progress(1, "Importing...");
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport report = importService.doImport(repoCollection, ADD_UPDATE_EXISTING, PACKAGE_DEFAULT);

		progress.status("Ingestion of url '" + url + "' done.");
		Integer count = report.getNrImportedEntitiesMap().get(entityTypeId);
		count = count != null ? count : 0;
		progress.progress(2, "Successfully imported " + count + " " + entityTypeId + " entities.");

		FileMeta fileMeta = createFileMeta(jobExecutionID, file);

		FileIngestJobExecution fileIngestJobExecution = (FileIngestJobExecution) progress.getJobExecution();
		fileIngestJobExecution.setFile(fileMeta);
		dataService.add(FILE_META, fileMeta);

		return fileMeta;
	}

	private FileMeta createFileMeta(String jobExecutionID, File file)
	{
		FileMeta fileMeta = fileMetaFactory.create(jobExecutionID);
		fileMeta.setContentType("text/csv");
		fileMeta.setSize(file.length());
		fileMeta.setFilename(jobExecutionID + '/' + file.getName());
		fileMeta.setUrl(FileDownloadController.URI + '/' + jobExecutionID);
		return fileMeta;
	}
}
