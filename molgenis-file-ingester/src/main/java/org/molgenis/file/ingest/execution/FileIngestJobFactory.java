package org.molgenis.file.ingest.execution;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
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
	public FileIngestJob createJob(FileIngestJobExecution fileIngestJobExecution)
	{
		dataService.add(FILE_INGEST_JOB_EXECUTION, fileIngestJobExecution);
		String username = fileIngestJobExecution.getUser();
		Progress progress = new ProgressImpl(fileIngestJobExecution, jobExecutionUpdater, mailSender);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);
		EntityType targetEntityEntity = fileIngestJobExecution.getTargetEntity();
		String targetEntityName = targetEntityEntity.getId();
		String url = fileIngestJobExecution.getUrl();
		String loader = fileIngestJobExecution.getLoader();
		String[] failureEmail = fileIngestJobExecution.getFailureEmail();

		return new FileIngestJob(progress, transactionTemplate, runAsAuthentication, fileIngester, targetEntityName,
				url, loader, failureEmail, fileIngestJobExecution.getIdentifier());
	}
}
