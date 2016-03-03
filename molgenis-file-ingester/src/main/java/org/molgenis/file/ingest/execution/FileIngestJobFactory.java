package org.molgenis.file.ingest.execution;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class FileIngestJobFactory
{
	private final DataService dataService;
	private final UserDetailsService userDetailsService;
	private final JobExecutionUpdater jobExecutionUpdater;
	private final PlatformTransactionManager transactionManager;
	private final FileIngester fileIngester;
	private final MailSender mailSender;

	@Autowired
	public FileIngestJobFactory(DataService dataService, UserDetailsService userDetailsService,
			JobExecutionUpdater jobExecutionUpdater, PlatformTransactionManager transactionManager,
			FileIngester fileIngester, MailSender mailSender)
	{
		this.dataService = requireNonNull(dataService);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.transactionManager = requireNonNull(transactionManager);
		this.fileIngester = requireNonNull(fileIngester);
		this.mailSender = requireNonNull(mailSender);
	}

	@RunAsSystem
	public FileIngestJob createJob(JobExecution metaData)
	{
		dataService.add(FileIngestJobExecutionMetaData.ENTITY_NAME, metaData);
		String username = metaData.getUser().getUsername();
		Progress progress = new ProgressImpl(metaData, jobExecutionUpdater, mailSender);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);
		Entity fileIngestEntity = metaData.getEntity(FileIngestJobExecutionMetaData.FILE_INGEST, JobExecution.class);
		Entity targetEntityEntity = fileIngestEntity.getEntity(FileIngestMetaData.ENTITY_META_DATA);
		String targetEntityName = targetEntityEntity.getString(EntityMetaDataMetaData.FULL_NAME);
		String url = fileIngestEntity.getString(FileIngestMetaData.URL);
		String loader = fileIngestEntity.getString(FileIngestMetaData.LOADER);
		String failureEmail = fileIngestEntity.getString(FileIngestMetaData.FAILURE_EMAIL);

		return new FileIngestJob(progress, transactionTemplate, runAsAuthentication, fileIngester, targetEntityName,
				url, loader, failureEmail, fileIngestEntity);
	}
}
