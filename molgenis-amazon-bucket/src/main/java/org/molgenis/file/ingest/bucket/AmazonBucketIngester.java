package org.molgenis.file.ingest.bucket;

import com.amazonaws.services.s3.AmazonS3;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jobs.Progress;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileStore;
import org.molgenis.file.ingest.bucket.client.AmazonBucketClient;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

import static java.util.Objects.requireNonNull;

@Component
public class AmazonBucketIngester
{
	private final ImportServiceFactory importServiceFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final FileMetaFactory fileMetaFactory;
	private final FileStore fileStore;
	private final AmazonBucketClient amazonBucketClient;

	@Autowired
	public AmazonBucketIngester(ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, FileMetaFactory fileMetaFactory,
			FileStore fileStore, AmazonBucketClient amazonBucketClient)
	{
		this.importServiceFactory = requireNonNull(importServiceFactory);
		this.fileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.fileStore = fileStore;
		this.amazonBucketClient = amazonBucketClient;
	}

	public FileMeta ingest(String jobExecutionID, String targetEntityTypeName, String bucket, String key,
			String profile, boolean isExpression, Progress progress)
	{
		FileMeta fileMeta;
		try
		{
			progress.setProgressMax(3);
			progress.progress(0, "Connection to Amazon Bucket with profile '" + profile + "'");
			AmazonS3 client = amazonBucketClient.getClient(profile);
			progress.progress(1, "downloading...");
			File file = amazonBucketClient.downloadFile(client, fileStore, jobExecutionID, bucket, key, isExpression);

			if (targetEntityTypeName != null) amazonBucketClient.renameSheet(targetEntityTypeName, file);
			progress.progress(2, "Importing...");
			ImportService importService = importServiceFactory.getImportService(file.getName());
			RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
					.createFileRepositoryCollection(file);
			EntityImportReport report = importService
					.doImport(repositoryCollection, DatabaseAction.ADD_UPDATE_EXISTING, "base");
			progress.status("Download and import from Amazon Bucket done.");
			progress.progress(3,
					"Successfully imported " + report.getNrImportedEntitiesMap().keySet().toString() + " entities.");
			fileMeta = createFileMeta(jobExecutionID, file);
		}
		catch (Exception e)
		{
			throw new MolgenisDataException(e);
		}
		return fileMeta;
	}

	private FileMeta createFileMeta(String jobExecutionID, File file)
	{
		FileMeta fileMeta = fileMetaFactory.create(jobExecutionID);
		fileMeta.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		fileMeta.setSize(file.length());
		fileMeta.setFilename(jobExecutionID + '/' + file.getName());
		fileMeta.setUrl(FileDownloadController.URI + '/' + jobExecutionID);
		return fileMeta;
	}
}