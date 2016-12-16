package org.molgenis.data.elasticsearch.index.job;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;

/**
 * Creates {@link IndexJob}s. Injects the beans they need to do their work.
 */
public class IndexJobFactory
{
	private DataService dataService;
	private SearchService searchService;
	@Autowired
	private JobExecutionUpdater jobExecutionUpdater;
	@Autowired
	private MailSender mailSender;

	public IndexJobFactory(DataService dataService, SearchService searchService)
	{
		this.dataService = dataService;
		this.searchService = searchService;
	}

	/**
	 * Create a IndexJob for a {@link IndexJobExecution} entity.
	 */
	public IndexJob createJob(IndexJobExecution indexJobExecution)
	{
		RunAsSystemProxy
				.runAsSystem(() -> dataService.add(IndexJobExecutionMeta.INDEX_JOB_EXECUTION, indexJobExecution));
		ProgressImpl progress = new ProgressImpl(indexJobExecution, jobExecutionUpdater, mailSender);
		return new IndexJob(progress, new SystemSecurityToken(), indexJobExecution.getIndexActionJobID(), dataService,
				searchService);
	}

}