package org.molgenis.data.elasticsearch.reindex.job;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;

/**
 * Creates {@link ReindexJob}s. Injects the beans they need to do their work.
 */
public class ReindexJobFactory
{
	private DataService dataService;
	private SearchService searchService;
	@Autowired
	private JobExecutionUpdater jobExecutionUpdater;
	@Autowired
	private MailSender mailSender;

	public ReindexJobFactory(DataService dataService, SearchService searchService)
	{
		this.dataService = dataService;
		this.searchService = searchService;
	}

	/**
	 * Create a ReindexJob for a {@link ReindexJobExecution} entity.
	 */
	ReindexJob createJob(ReindexJobExecution reindexJobExecution)
	{
		RunAsSystemProxy
				.runAsSystem(() -> dataService.add(ReindexJobExecutionMeta.REINDEX_JOB_EXECUTION, reindexJobExecution));
		ProgressImpl progress = new ProgressImpl(reindexJobExecution, jobExecutionUpdater, mailSender);
		return new ReindexJob(progress, new SystemSecurityToken(), reindexJobExecution.getReindexActionJobID(),
				dataService, searchService);
	}

}