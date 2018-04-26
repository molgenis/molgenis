package org.molgenis.amazon.bucket;

import com.amazonaws.services.s3.AmazonS3;
import org.molgenis.amazon.bucket.client.AmazonBucketClient;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.excel.ExcelUtils;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.jobs.Progress;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static java.util.Objects.requireNonNull;

@Component
public class AmazonBucketIngester
{
	private final ImportServiceFactory importServiceFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final FileMetaFactory fileMetaFactory;
	private final FileStore fileStore;
	private final AmazonBucketClient amazonBucketClient;

	public AmazonBucketIngester(ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, FileMetaFactory fileMetaFactory,
			FileStore fileStore, AmazonBucketClient amazonBucketClient)
	{
		this.importServiceFactory = requireNonNull(importServiceFactory);
		this.fileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.fileStore = requireNonNull(fileStore);
		this.amazonBucketClient = requireNonNull(amazonBucketClient);
	}

	public FileMeta ingest(String jobExecutionID, String targetEntityTypeName, String bucket, String key,
			String extension, String accessKey, String secretKey, String region, boolean isExpression,
			Progress progress)
	{
		FileMeta fileMeta;
		try
		{
			progress.setProgressMax(3);
			progress.progress(0, "Connection to Amazon Bucket with accessKey '" + accessKey + "'");
			AmazonS3 client = amazonBucketClient.getClient(accessKey, secretKey, region);
			progress.progress(1, "downloading...");
			File file = amazonBucketClient.downloadFile(client, fileStore, jobExecutionID, bucket, key, extension,
					isExpression, targetEntityTypeName);
			if (targetEntityTypeName != null && ExcelUtils.isExcelFile(file.getName()))
			{
				if (ExcelUtils.getNumberOfSheets(file) == 1)
				{
					ExcelUtils.renameSheet(targetEntityTypeName, file, 0);
				}
				else
				{
					throw new MolgenisDataException(
							"Amazon Bucket imports to a specified entityType are only possible with CSV files or Excel files with one sheet");
				}
			}
			progress.progress(2, "Importing...");
			ImportService importService = importServiceFactory.getImportService(file.getName());
			File renamed = new File(
					String.format("%s%s%s.%s", file.getParent(), File.separatorChar, targetEntityTypeName, extension));
			Files.copy(file.toPath(), renamed.toPath(), StandardCopyOption.REPLACE_EXISTING);
			RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(
					renamed);
			EntityImportReport report = importService.doImport(repositoryCollection, DatabaseAction.ADD_UPDATE_EXISTING,
					"base");
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
		fileMeta.setUrl("/files/" + jobExecutionID);
		return fileMeta;
	}
}