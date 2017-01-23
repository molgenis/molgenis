package org.molgenis.data.elasticsearch.index;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.index.job.IndexJobExecutionFactory;
import org.molgenis.data.elasticsearch.index.job.IndexJobFactory;
import org.molgenis.data.elasticsearch.index.job.IndexService;
import org.molgenis.data.elasticsearch.index.job.IndexServiceImpl;
import org.molgenis.data.elasticsearch.transaction.IndexTransactionListener;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.JobExecutionUpdaterImpl;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.molgenis.data.elasticsearch.index.job.IndexJobExecutionMeta.INDEX_JOB_EXECUTION;

@Configuration
@ComponentScan(basePackages = { "org.molgenis.data.elasticsearch.index.job, org.molgenis.data.jobs.model" })
public class IndexConfig
{
	@Autowired
	private IndexActionRegisterService indexActionRegisterService;

	@Autowired
	private MolgenisTransactionManager molgenisTransactionManager;

	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private IndexJobExecutionFactory indexJobExecutionFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	@PostConstruct
	public void register()
	{
		indexActionRegisterService.addExcludedEntity(INDEX_JOB_EXECUTION);
	}

	@Bean
	public IndexTransactionListener indexTransactionListener()
	{
		final IndexTransactionListener indexTransactionListener = new IndexTransactionListener(rebuildIndexService(),
				indexActionRegisterService);
		molgenisTransactionManager.addTransactionListener(indexTransactionListener);
		return indexTransactionListener;
	}

	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return new JobExecutionUpdaterImpl();
	}

	@Bean
	public IndexJobFactory indexJobFactory()
	{
		return new IndexJobFactory(dataService, searchService, entityTypeFactory);
	}

	@Bean
	public IndexService rebuildIndexService()
	{
		return new IndexServiceImpl(dataService, indexJobFactory(), indexJobExecutionFactory, executorService);
	}

}
