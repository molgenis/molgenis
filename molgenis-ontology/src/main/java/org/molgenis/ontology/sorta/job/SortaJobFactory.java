package org.molgenis.ontology.sorta.job;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SortaJobFactory
{
	@Autowired
	private DataService dataService;

	@Autowired
	private SortaService sortaService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private JobExecutionUpdater jobExecutionUpdater;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private MailSender mailSender;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private MenuReaderService menuReaderService;

	@RunAsSystem
	public SortaJobImpl create(SortaJobExecution jobExecution)
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

		ProgressImpl progress = new ProgressImpl(jobExecution, jobExecutionUpdater, mailSender);

		String username = jobExecution.getUser();
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);

		SortaJobProcessor matchInputTermBatchService = new SortaJobProcessor(jobExecution.getOntologyIri(),
				jobExecution.getSourceEntityName(), jobExecution.getResultEntityName(), progress, dataService,
				sortaService, idGenerator, menuReaderService);

		return new SortaJobImpl(matchInputTermBatchService, runAsAuthentication, progress, transactionTemplate);
	}
}
