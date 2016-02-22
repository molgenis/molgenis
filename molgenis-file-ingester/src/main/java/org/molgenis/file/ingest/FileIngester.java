package org.molgenis.file.ingest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Package;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jobs.JobMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileMeta;
import org.molgenis.file.FileStore;
import org.molgenis.file.ingest.meta.FileIngestJobMetaDataMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Imports a file from a remote url.
 * 
 * For now only csv files are supported
 */
@Component
public class FileIngester
{
	private static final Logger LOG = LoggerFactory.getLogger(FileIngester.class);
	private final FileStore fileStore;
	private final ImportServiceFactory importServiceFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final DataService dataService;
	private final FileIngestJobMetaDataMetaData fileIngestJobMetaDataMetaData;
	private final JavaMailSender mailSender;

	@Autowired
	public FileIngester(FileStore fileStore, ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, DataService dataService,
			FileIngestJobMetaDataMetaData fileIngestJobMetaDataMetaData, JavaMailSender mailSender)
	{
		this.fileStore = fileStore;
		this.importServiceFactory = importServiceFactory;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.dataService = dataService;
		this.fileIngestJobMetaDataMetaData = fileIngestJobMetaDataMetaData;
		this.mailSender = mailSender;
	}

	/**
	 * Imports a csv file defined in the fileIngest entity
	 * 
	 * @see FileIngestMetaData
	 * 
	 * @param fileIngest
	 */
	public void ingest(Entity fileIngest)
	{
		String url = fileIngest.getString(FileIngestMetaData.URL);
		Entity jobMeta = null;
		try
		{
			LOG.info("Ingesting url '{}'", url);
			jobMeta = createFileIngestJobMetaData(fileIngest);

			String loader = fileIngest.getString(FileIngestMetaData.LOADER);
			if (!"CSV".equals(loader))
			{
				throw new FileIngestException("Unknown loader '" + loader + "'");
			}

			String entityName = fileIngest.getString(FileIngestMetaData.ENTITY_META_DATA);
			File file = downloadCsvFile(url, entityName, jobMeta.getString(JobMetaData.IDENTIFIER));
			logDownloadFinished(jobMeta, file, "text/csv");

			FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory
					.createFileRepositoryCollection(file);
			ImportService importService = importServiceFactory.getImportService(file, repoCollection);
			importService.doImport(repoCollection, DatabaseAction.ADD_UPDATE_EXISTING, Package.DEFAULT_PACKAGE_NAME);

			logSuccess(jobMeta);
			LOG.info("Ingestion of url '{}' done.", url);
		}
		catch (Exception e)
		{
			LOG.error("Error ingesting url '" + url + "'", e);

			if (jobMeta != null)
			{
				logFailure(jobMeta, e);
			}

			String email = fileIngest.getString(FileIngestMetaData.FAILURE_EMAIL);
			if (StringUtils.isNotBlank(email))
			{
				emailFailure(email, url, e);
			}
		}
	}

	private void emailFailure(String email, String url, Exception e)
	{
		try
		{
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(email);
			mailMessage.setSubject("Molgenis import failed");
			mailMessage.setText("The scheduled import of url '" + url + "' failed. Error:\n" + e.getMessage());
			mailSender.send(mailMessage);
		}
		catch (MailException mce)
		{
			LOG.error("Could not send error email", e);
		}
	}

	private void logFailure(Entity jobMetaData, Exception e)
	{
		jobMetaData.set(JobMetaData.STATUS, "FAILED");
		jobMetaData.set(JobMetaData.PROGRESS_MESSAGE, "Import failed. Errormessage:" + e.getMessage());
		jobMetaData.set(JobMetaData.END_DATE, new Date());
		dataService.update(fileIngestJobMetaDataMetaData.getName(), jobMetaData);
	}

	private void logSuccess(Entity jobMetaData)
	{
		jobMetaData.set(JobMetaData.STATUS, "SUCCESS");
		jobMetaData.set(JobMetaData.PROGRESS_MESSAGE, "Import successfully completed.");
		jobMetaData.set(JobMetaData.END_DATE, new Date());
		dataService.update(fileIngestJobMetaDataMetaData.getName(), jobMetaData);
	}

	private void logDownloadFinished(Entity jobMetaData, File file, String contentType)
	{
		String id = jobMetaData.getString(JobMetaData.IDENTIFIER);

		FileMeta fileMeta = new FileMeta(dataService);
		fileMeta.setId(id);
		fileMeta.setContentType(contentType);
		fileMeta.setSize(file.length());
		fileMeta.setFilename(id + '/' + file.getName());
		fileMeta.setUrl(FileDownloadController.URI + '/' + id);
		dataService.add(FileMeta.ENTITY_NAME, fileMeta);
		
		jobMetaData.set(FileIngestJobMetaDataMetaData.FILE, fileMeta);
		jobMetaData.set(JobMetaData.PROGRESS_MESSAGE, "Importing...");
		dataService.update(fileIngestJobMetaDataMetaData.getName(), jobMetaData);
	}

	private Entity createFileIngestJobMetaData(Entity fileIngest)
	{
		Entity entity = new DefaultEntity(fileIngestJobMetaDataMetaData, dataService);
		entity.set(JobMetaData.PROGRESS_MESSAGE,
				"Downloading file from '" + fileIngest.getString(FileIngestMetaData.URL) + "'");
		entity.set(JobMetaData.START_DATE, new Date());
		entity.set(JobMetaData.SUBMISSION_DATE, new Date());
		entity.set(JobMetaData.STATUS, "RUNNING");
		entity.set(JobMetaData.TYPE, "FileIngesterJob");
		entity.set(JobMetaData.USER, dataService.query(MolgenisUser.ENTITY_NAME).eq(MolgenisUser.USERNAME, "admin")
				.findOne());// TODO system user?
		entity.set(FileIngestJobMetaDataMetaData.FILE_INGEST, fileIngest);

		dataService.add(fileIngestJobMetaDataMetaData.getName(), entity);

		return entity;
	}

	private File downloadCsvFile(String url, String entityName, String folderName) throws MalformedURLException,
			IOException
	{
		InputStream in = new URL(url).openStream();
		File folder = new File(fileStore.getStorageDir(), folderName);
		folder.mkdir();

		String filename = folderName + '/' + entityName + ".csv";

		return fileStore.store(in, filename);
	}
}
