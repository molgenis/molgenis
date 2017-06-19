package org.molgenis.data.elasticsearch.index;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.index.job.*;
import org.molgenis.data.elasticsearch.transaction.IndexTransactionListener;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.SearchService;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobExecutor;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static org.molgenis.data.elasticsearch.index.job.IndexJobExecutionMeta.INDEX_JOB_EXECUTION;

@Configuration
@ComponentScan(basePackages = { "org.molgenis.data.elasticsearch.index.job, org.molgenis.data.jobs.model" })
public class IndexConfig
{
	@Autowired
	private IndexActionRegisterService indexActionRegisterService;

	@Autowired
	private TransactionManager transactionManager;

	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private IndexJobExecutionFactory indexJobExecutionFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private JobExecutor jobExecutor;

	@PostConstruct
	public void register()
	{
		indexActionRegisterService.addExcludedEntity(INDEX_JOB_EXECUTION);
	}

	@Bean
	public IndexTransactionListener indexTransactionListener()
	{
		final IndexTransactionListener indexTransactionListener = new IndexTransactionListener(indexJobScheduler(),
				indexActionRegisterService);
		transactionManager.addTransactionListener(indexTransactionListener);
		return indexTransactionListener;
	}

	@Bean
	public IndexJobScheduler indexJobScheduler()
	{
		return new IndexJobSchedulerImpl(dataService, indexJobExecutionFactory, jobExecutor);
	}

	@Bean
	public IndexJobService indexJobService()
	{
		return new IndexJobService(dataService, searchService, entityTypeFactory);
	}

	@Bean
	public JobFactory<IndexJobExecution> indexJobFactory()
	{
		return new JobFactory<IndexJobExecution>()
		{
			@Override
			public Job<Void> createJob(IndexJobExecution jobExecution)
			{
				return progress -> indexJobService().executeJob(progress, jobExecution.getIndexActionJobID());
			}
		};
	}
}
