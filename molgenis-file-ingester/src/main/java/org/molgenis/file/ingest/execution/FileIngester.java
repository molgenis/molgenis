package org.molgenis.file.ingest.execution;

import com.amazonaws.services.s3.AmazonS3;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.ingest.amazon.BucketUtils;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.file.ingest.meta.FileIngestType;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.DatabaseAction.ADD_UPDATE_EXISTING;
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

	@Autowired
	public FileIngester(FileStoreDownload fileStoreDownload, ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, FileMetaFactory fileMetaFactory)
	{
		this.fileStoreDownload = requireNonNull(fileStoreDownload);
		this.importServiceFactory = requireNonNull(importServiceFactory);
		this.fileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
	}

	/**
	 * Imports a csv file defined in the fileIngest entity
	 *
	 * @see FileIngestMetaData
	 */
	public FileMeta ingest(String entityName, String url, String loader, String jobExecutionID, Progress progress,
			String failureEmail, String bucket, String key, String profile, FileIngestType type)
	{
		FileMeta fileMeta = null;
		try
		{
			if (type == FileIngestType.DOWNLOAD)
			{
				if (loader == null) throw new FileIngestException("Missing the required field 'loader'");
				if (url == null) throw new FileIngestException("Missing the required field 'URL'");
				if (!"CSV".equals(loader)) throw new FileIngestException("Unknown loader '" + loader + "'");

				progress.setProgressMax(2);
				progress.progress(0, "Downloading url '" + url + "'");
				File file = fileStoreDownload.downloadFile(url, jobExecutionID, entityName + ".csv");
				progress.progress(1, "Importing...");
				FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory
						.createFileRepositoryCollection(file);
				ImportService importService = importServiceFactory.getImportService(file, repoCollection);
				EntityImportReport report = importService
						.doImport(repoCollection, ADD_UPDATE_EXISTING, PACKAGE_DEFAULT);

				progress.status("Ingestion of url '" + url + "' done.");
				Integer count = report.getNrImportedEntitiesMap().get(entityName);
				count = count != null ? count : 0;
				progress.progress(2, "Successfully imported " + count + " " + entityName + " entities.");
				fileMeta = createFileMeta(jobExecutionID, file);
			}
			else if (type == FileIngestType.BUCKET)
			{
				progress.setProgressMax(3);
				progress.progress(0, "Connection to Amazon Bucket with profile '" + profile + "'");
				AmazonS3 client = BucketUtils.getClient(profile);
				File temp = File.createTempFile("bucket_", ".xlsx");
				progress.progress(1, "downloading...");
				File file = BucketUtils.downloadFile(client, temp, bucket, key, true);
				progress.progress(2, "Importing...");
				ImportService importService = importServiceFactory.getImportService(file.getName());
				RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
						.createFileRepositoryCollection(file);
				EntityImportReport report = importService
						.doImport(repositoryCollection, DatabaseAction.ADD_UPDATE_EXISTING, "base");
				progress.status("Download and import from Amazon Bucket done.");
				progress.progress(3, "Successfully imported " + report.getNrImportedEntitiesMap().keySet().toString()
						+ " entities.");
				fileMeta = createFileMeta(jobExecutionID, file);
			}
		}
		catch (Exception e)
		{
			//FIXME: send failure email
			throw new FileIngestException(e);
		}
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
