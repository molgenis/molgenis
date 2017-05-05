package org.molgenis.file.ingest.execution;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.schedule.MolgenisJobFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE_INGEST_JOB_EXECUTION;

@Component
public class FileIngestJobFactory implements MolgenisJobFactory
{
	private final DataService dataService;
	private final UserDetailsService userDetailsService;
	private final JobExecutionUpdater jobExecutionUpdater;
	private final PlatformTransactionManager transactionManager;
	private final FileIngester fileIngester;
	private final MailSender mailSender;
	private final FileIngestJobExecutionMetaData fileIngestJobExecutionMetaData;

	@Autowired
	public FileIngestJobFactory(DataService dataService, UserDetailsService userDetailsService,
			JobExecutionUpdater jobExecutionUpdater, PlatformTransactionManager transactionManager,
			FileIngester fileIngester, MailSender mailSender,
			FileIngestJobExecutionMetaData fileIngestJobExecutionMetaData)
	{
		this.dataService = requireNonNull(dataService);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.transactionManager = requireNonNull(transactionManager);
		this.fileIngester = requireNonNull(fileIngester);
		this.mailSender = requireNonNull(mailSender);
		this.fileIngestJobExecutionMetaData = requireNonNull(fileIngestJobExecutionMetaData);
	}

	@RunAsSystem
	public FileIngestJob createJob(JobExecution jobExecution)
	{
		dataService.add(FILE_INGEST_JOB_EXECUTION, jobExecution);
		String username = jobExecution.getUser();
		Progress progress = new ProgressImpl(jobExecution, jobExecutionUpdater, mailSender);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);

		FileIngestJobExecution fileIngestJobExecution = (FileIngestJobExecution) jobExecution;
		String targetEntityId = fileIngestJobExecution.getTargetEntityId();
		String url = fileIngestJobExecution.getUrl();
		String loader = fileIngestJobExecution.getLoader();
		String[] failureEmail = fileIngestJobExecution.getFailureEmail();

		return new FileIngestJob(progress, transactionTemplate, runAsAuthentication, fileIngester, targetEntityId,
				url, loader, failureEmail, fileIngestJobExecution.getIdentifier());
	}

	@Override
	public EntityType getJobExecutionType()
	{
		return fileIngestJobExecutionMetaData;
	}
}
