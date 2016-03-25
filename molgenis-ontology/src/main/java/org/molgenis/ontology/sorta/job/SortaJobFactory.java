package org.molgenis.ontology.sorta.job;

import org.molgenis.data.DataService;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.core.context.SecurityContext;
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

	@RunAsSystem
	public SortaJobImpl create(SortaJobExecution jobExecution, SecurityContext securityContext)
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

		ProgressImpl progress = new ProgressImpl(jobExecution, jobExecutionUpdater, mailSender);

		SortaJobProcessor matchInputTermBatchService = new SortaJobProcessor(jobExecution.getOntologyIri(),
				jobExecution.getSourceEntityName(), jobExecution.getResultEntityName(), progress, dataService,
				sortaService, idGenerator);

		return new SortaJobImpl(matchInputTermBatchService, securityContext, progress, transactionTemplate);
	}
}
