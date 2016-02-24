package org.molgenis.file.ingest.impl;

import java.io.File;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.JobMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileMeta;
import org.molgenis.file.ingest.FileIngesterLogger;
import org.molgenis.file.ingest.meta.FileIngestJobMetaDataMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.framework.db.EntityImportReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class FileIngesterLoggerImpl implements FileIngesterLogger
{
	private static final Logger LOG = LoggerFactory.getLogger(FileIngesterLoggerImpl.class);

	private final DataService dataService;
	private final FileIngestJobMetaDataMetaData fileIngestJobMetaDataMetaData;
	private final JavaMailSender mailSender;
	private Entity fileIngestJobMetaData;
	private String entityName;
	private String failureEmail;
	private String downloadUrl;

	public FileIngesterLoggerImpl(DataService dataService, FileIngestJobMetaDataMetaData fileIngestJobMetaDataMetaData,
			JavaMailSender mailSender)
	{
		this.dataService = dataService;
		this.fileIngestJobMetaDataMetaData = fileIngestJobMetaDataMetaData;
		this.mailSender = mailSender;
	}

	@Override
	public synchronized String start(Entity fileIngest)
	{
		Entity entityMetaData = fileIngest.getEntity(FileIngestMetaData.ENTITY_META_DATA);
		entityName = entityMetaData.getString(EntityMetaDataMetaData.FULL_NAME);
		failureEmail = fileIngest.getString(FileIngestMetaData.FAILURE_EMAIL);
		downloadUrl = fileIngest.getString(FileIngestMetaData.URL);

		fileIngestJobMetaData = new DefaultEntity(fileIngestJobMetaDataMetaData, dataService);
		fileIngestJobMetaData.set(JobMetaData.PROGRESS_MESSAGE,
				"Downloading file from '" + fileIngest.getString(FileIngestMetaData.URL) + "'");
		fileIngestJobMetaData.set(JobMetaData.START_DATE, new Date());
		fileIngestJobMetaData.set(JobMetaData.SUBMISSION_DATE, new Date());
		fileIngestJobMetaData.set(JobMetaData.STATUS, "RUNNING");
		fileIngestJobMetaData.set(JobMetaData.TYPE, "FileIngesterJob");
		fileIngestJobMetaData.set(JobMetaData.USER,
				dataService.query(MolgenisUser.ENTITY_NAME).eq(MolgenisUser.USERNAME, "admin").findOne());// TODO system
																											// user?
		fileIngestJobMetaData.set(FileIngestJobMetaDataMetaData.FILE_INGEST, fileIngest);

		dataService.add(fileIngestJobMetaDataMetaData.getName(), fileIngestJobMetaData);
		
		return fileIngestJobMetaData.getString(JobMetaData.IDENTIFIER);
	}

	@Override
	public synchronized void downloadFinished(File file, String contentType)
	{
		String id = fileIngestJobMetaData.getString(JobMetaData.IDENTIFIER);

		FileMeta fileMeta = new FileMeta(dataService);
		fileMeta.setId(id);
		fileMeta.setContentType(contentType);
		fileMeta.setSize(file.length());
		fileMeta.setFilename(id + '/' + file.getName());
		fileMeta.setUrl(FileDownloadController.URI + '/' + id);
		dataService.add(FileMeta.ENTITY_NAME, fileMeta);

		fileIngestJobMetaData.set(FileIngestJobMetaDataMetaData.FILE, fileMeta);
		fileIngestJobMetaData.set(JobMetaData.PROGRESS_MESSAGE, "Importing...");
		dataService.update(fileIngestJobMetaDataMetaData.getName(), fileIngestJobMetaData);
	}

	@Override
	public synchronized void success(EntityImportReport report)
	{
		Integer count = report.getNrImportedEntitiesMap().get(entityName);
		count = count != null ? count : 0;
		fileIngestJobMetaData.set(JobMetaData.STATUS, "SUCCESS");
		fileIngestJobMetaData.set(JobMetaData.PROGRESS_MESSAGE,
				String.format("Successfully imported %d %s entities.", count, entityName));
		fileIngestJobMetaData.set(JobMetaData.END_DATE, new Date());
		dataService.update(fileIngestJobMetaDataMetaData.getName(), fileIngestJobMetaData);
	}

	@Override
	public synchronized void failure(Exception e)
	{
		try
		{
			fileIngestJobMetaData.set(JobMetaData.STATUS, "FAILED");
			fileIngestJobMetaData.set(JobMetaData.PROGRESS_MESSAGE, "Import failed. Errormessage:" + e.getMessage());
			fileIngestJobMetaData.set(JobMetaData.END_DATE, new Date());
			dataService.update(fileIngestJobMetaDataMetaData.getName(), fileIngestJobMetaData);
		}
		finally
		{
			if (StringUtils.isNotBlank(failureEmail))
			{
				emailFailure(failureEmail, downloadUrl, e);
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

}
