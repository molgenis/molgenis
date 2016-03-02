package org.molgenis.file.ingest.impl;

import java.io.File;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.JobExecution;
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
		fileIngestJobMetaData.set(JobExecution.PROGRESS_MESSAGE,
				"Downloading file from '" + fileIngest.getString(FileIngestMetaData.URL) + "'");
		fileIngestJobMetaData.set(JobExecution.START_DATE, new Date());
		fileIngestJobMetaData.set(JobExecution.SUBMISSION_DATE, new Date());
		fileIngestJobMetaData.set(JobExecution.STATUS, "RUNNING");
		fileIngestJobMetaData.set(JobExecution.TYPE, "FileIngesterJob");
		fileIngestJobMetaData.set(JobExecution.USER,
				dataService.query(MolgenisUser.ENTITY_NAME).eq(MolgenisUser.USERNAME, "admin").findOne());// TODO system
																											// user?
		fileIngestJobMetaData.set(FileIngestJobMetaDataMetaData.FILE_INGEST, fileIngest);

		dataService.add(fileIngestJobMetaDataMetaData.getName(), fileIngestJobMetaData);

		return fileIngestJobMetaData.getString(JobExecution.IDENTIFIER);
	}

	@Override
	public synchronized void downloadFinished(File file, String contentType)
	{
		String id = fileIngestJobMetaData.getString(JobExecution.IDENTIFIER);

		FileMeta fileMeta = new FileMeta(dataService);
		fileMeta.setId(id);
		fileMeta.setContentType(contentType);
		fileMeta.setSize(file.length());
		fileMeta.setFilename(id + '/' + file.getName());
		fileMeta.setUrl(FileDownloadController.URI + '/' + id);
		dataService.add(FileMeta.ENTITY_NAME, fileMeta);

		fileIngestJobMetaData.set(FileIngestJobMetaDataMetaData.FILE, fileMeta);
		fileIngestJobMetaData.set(JobExecution.PROGRESS_MESSAGE, "Importing...");
		dataService.update(fileIngestJobMetaDataMetaData.getName(), fileIngestJobMetaData);
	}

	@Override
	public synchronized void success(EntityImportReport report)
	{
		Integer count = report.getNrImportedEntitiesMap().get(entityName);
		count = count != null ? count : 0;
		fileIngestJobMetaData.set(JobExecution.STATUS, "SUCCESS");
		fileIngestJobMetaData.set(JobExecution.PROGRESS_MESSAGE,
				String.format("Successfully imported %d %s entities.", count, entityName));
		fileIngestJobMetaData.set(JobExecution.END_DATE, new Date());
		dataService.update(fileIngestJobMetaDataMetaData.getName(), fileIngestJobMetaData);
	}

	@Override
	public synchronized void failure(Exception e)
	{
		try
		{
			fileIngestJobMetaData.set(JobExecution.STATUS, "FAILED");
			fileIngestJobMetaData.set(JobExecution.PROGRESS_MESSAGE, "Import failed. Errormessage:" + e.getMessage());
			fileIngestJobMetaData.set(JobExecution.END_DATE, new Date());
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
