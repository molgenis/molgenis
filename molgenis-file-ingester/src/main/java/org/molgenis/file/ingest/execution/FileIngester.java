package org.molgenis.file.ingest.execution;

import static java.util.Objects.requireNonNull;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileMeta;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
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
	private final MailSender mailSender;

	@Autowired
	public FileIngester(FileStoreDownload fileStoreDownload, ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, DataService dataService,
			MailSender mailSender)
	{
		this.fileStoreDownload = requireNonNull(fileStoreDownload);
		this.importServiceFactory = requireNonNull(importServiceFactory);
		this.fileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
		this.dataService = requireNonNull(dataService);
		this.mailSender = requireNonNull(mailSender);
	}

	/**
	 * Imports a csv file defined in the fileIngest entity
	 * 
	 * @see FileIngestMetaData
	 * 
	 * @param fileIngest
	 */
	public void ingest(String entityName, String url, String loader, String jobExecutionID, Progress progress,
			String failureEmail, Entity fileIngestJobExecution)
	{
		try
		{
			if (!"CSV".equals(loader))
			{
				throw new FileIngestException("Unknown loader '" + loader + "'");
			}

			progress.setProgressMax(2);
			progress.progress(0, "Ingesting url '" + url + "'");
			File file = fileStoreDownload.downloadFile(url, jobExecutionID, entityName + ".csv");
			storeFileMeta(jobExecutionID, dataService, file, fileIngestJobExecution);

			progress.progress(1, "Importing...");
			FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory
					.createFileRepositoryCollection(file);
			ImportService importService = importServiceFactory.getImportService(file, repoCollection);
			EntityImportReport report = importService.doImport(repoCollection, DatabaseAction.ADD_UPDATE_EXISTING,
					Package.DEFAULT_PACKAGE_NAME);

			progress.status("Ingestion of url '" + url + "' done.");
			Integer count = report.getNrImportedEntitiesMap().get(entityName);
			count = count != null ? count : 0;
			progress.progress(2, "Successfully imported " + count + " " + entityName + " entities.");
		}
		catch (Exception ex)
		{
			if (StringUtils.isNotBlank(failureEmail))
			{
				emailFailure(failureEmail, url, ex);
			}
			throw ex;
		}
	}

	private void storeFileMeta(String jobExecutionID, DataService dataService, File file, Entity fileIngestJobExecution)
	{
		FileMeta fileMeta = new FileMeta(dataService);
		fileMeta.setId(jobExecutionID);
		fileMeta.setContentType("text/csv");
		fileMeta.setSize(file.length());
		fileMeta.setFilename(jobExecutionID + '/' + file.getName());
		fileMeta.setUrl(FileDownloadController.URI + '/' + jobExecutionID);
		dataService.add(FileMeta.ENTITY_NAME, fileMeta);
		// set xref to uploaded file in job execution entity
		fileIngestJobExecution.set(FileIngestJobExecutionMetaData.FILE, fileMeta);
	}

	private void emailFailure(String email, String url, Exception e) throws MailException
	{
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(email);
		mailMessage.setSubject("Molgenis import failed");
		mailMessage.setText("The scheduled import of url '" + url + "' failed. Error:\n" + e.getMessage());
		mailSender.send(mailMessage);
	}
}
