package org.molgenis.file.ingest.execution;

import static java.util.Objects.requireNonNull;

import java.io.File;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileMeta;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Imports a file from a remote url.
 * 
 * For now only csv files are supported
 */
@Component
public class FileIngester
{
	private final FileStoreDownload fileStoreDownload;
	private final ImportServiceFactory importServiceFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final DataService dataService;

	@Autowired
	public FileIngester(FileStoreDownload fileStoreDownload, ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, DataService dataService)
	{
		this.fileStoreDownload = requireNonNull(fileStoreDownload);
		this.importServiceFactory = requireNonNull(importServiceFactory);
		this.fileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
		this.dataService = requireNonNull(dataService);
	}

	/**
	 * Imports a csv file defined in the fileIngest entity
	 * 
	 * @see FileIngestMetaData
	 * 
	 * @param fileIngest
	 */
	public FileMeta ingest(String entityName, String url, String loader, String jobExecutionID, Progress progress,
			String failureEmail)
	{
		if (!"CSV".equals(loader))
		{
			throw new FileIngestException("Unknown loader '" + loader + "'");
		}

		progress.setProgressMax(2);
		progress.progress(0, "Downloading url '" + url + "'");
		File file = fileStoreDownload.downloadFile(url, jobExecutionID, entityName + ".csv");
		progress.progress(1, "Importing...");
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport report = importService.doImport(repoCollection, DatabaseAction.ADD_UPDATE_EXISTING,
				Package.DEFAULT_PACKAGE_NAME);

		progress.status("Ingestion of url '" + url + "' done.");
		Integer count = report.getNrImportedEntitiesMap().get(entityName);
		count = count != null ? count : 0;
		progress.progress(2, "Successfully imported " + count + " " + entityName + " entities.");
		FileMeta fileMeta = createFileMeta(jobExecutionID, dataService, file);
		return fileMeta;
	}

	private FileMeta createFileMeta(String jobExecutionID, DataService dataService, File file)
	{
		FileMeta fileMeta = new FileMeta(dataService);
		fileMeta.setId(jobExecutionID);
		fileMeta.setContentType("text/csv");
		fileMeta.setSize(file.length());
		fileMeta.setFilename(jobExecutionID + '/' + file.getName());
		fileMeta.setUrl(FileDownloadController.URI + '/' + jobExecutionID);
		return fileMeta;
	}
}
