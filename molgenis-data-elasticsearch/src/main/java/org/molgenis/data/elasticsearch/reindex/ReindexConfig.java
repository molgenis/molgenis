package org.molgenis.data.elasticsearch.reindex;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.reindex.job.ReindexJobExecutionFactory;
import org.molgenis.data.elasticsearch.reindex.job.ReindexJobFactory;
import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.elasticsearch.reindex.job.ReindexServiceImpl;
import org.molgenis.data.elasticsearch.transaction.ReindexTransactionListener;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.JobExecutionUpdaterImpl;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta.REINDEX_JOB_EXECUTION;

@Configuration
@ComponentScan(basePackages = { "org.molgenis.data.elasticsearch.reindex, org.molgenis.data.jobs.model" })
public class ReindexConfig
{
	@Autowired
	private ReindexActionRegisterService reindexActionRegisterService;

	@Autowired
	private MolgenisTransactionManager molgenisTransactionManager;

	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private ReindexJobExecutionFactory reindexJobExecutionFactory;

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	@PostConstruct
	public void register()
	{
		reindexActionRegisterService.addExcludedEntity(REINDEX_JOB_EXECUTION);
	}

	@Bean
	public ReindexTransactionListener reindexTransactionListener()
	{
		final ReindexTransactionListener reindexTransactionListener = new ReindexTransactionListener(
				rebuildIndexService(), reindexActionRegisterService);
		molgenisTransactionManager.addTransactionListener(reindexTransactionListener);
		return reindexTransactionListener;
	}

	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return new JobExecutionUpdaterImpl();
	}

	@Bean
	public ReindexJobFactory reindexJobFactory()
	{
		return new ReindexJobFactory(dataService, searchService);
	}

	@Bean
	public ReindexService rebuildIndexService()
	{
		return new ReindexServiceImpl(dataService, reindexJobFactory(), reindexJobExecutionFactory, executorService);
	}

}
