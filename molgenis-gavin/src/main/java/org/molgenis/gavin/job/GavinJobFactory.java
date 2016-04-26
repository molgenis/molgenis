package org.molgenis.gavin.job;

import org.molgenis.data.DataService;
import org.molgenis.data.annotation.CrudRepositoryAnnotator;
import org.molgenis.data.annotation.EffectsAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CmdLineAnnotator;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.molgenis.gavin.job.GavinJobExecutionMetaData.GAVIN_JOB_EXECUTION;

@Component
public class GavinJobFactory
{
	@Autowired
	CrudRepositoryAnnotator crudRepositoryAnnotator;

	@Autowired
	DataService dataService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JobExecutionUpdater jobExecutionUpdater;

	@Autowired
	private MailSender mailSender;

	@Autowired
	FileStore fileStore;

	@Autowired
	private RepositoryAnnotator cadd;

	@Autowired
	private RepositoryAnnotator exac;

	@Autowired
	private RepositoryAnnotator snpEff;

	@Autowired
	private EffectsAnnotator gavin;

	@Autowired
	private MenuReaderService menuReaderService;

	@RunAsSystem
	public GavinJob createJob(GavinJobExecution metaData)
	{
		dataService.add(GAVIN_JOB_EXECUTION, metaData);
		String username = metaData.getUser().getUsername();

		// create an authentication to run as the user that is listed as the owner of the job
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);

		return new GavinJob(new CmdLineAnnotator(), new ProgressImpl(metaData, jobExecutionUpdater, mailSender),
				new TransactionTemplate(transactionManager), runAsAuthentication, metaData.getIdentifier(), fileStore,
				menuReaderService, cadd, exac, snpEff, gavin);
	}

	public List<String> getAnnotatorsWithMissingResources()
	{
		return of(cadd, exac, snpEff, gavin).filter(annotator -> !annotator.annotationDataExists())
				.map(RepositoryAnnotator::getSimpleName).collect(toList());
	}
}
