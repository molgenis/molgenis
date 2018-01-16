package org.molgenis.data.index;

import org.molgenis.data.DataService;
import org.molgenis.data.index.job.*;
import org.molgenis.data.index.meta.*;
import org.molgenis.data.index.transaction.IndexTransactionListener;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.jobs.JobFactory;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

import static org.molgenis.data.index.job.IndexJobExecutionMeta.INDEX_JOB_EXECUTION;

//TODO: These imported classes should be in separate config and this is the IndexJobConfig
@Import({ IndexActionFactory.class, IndexActionGroupFactory.class, IndexActionMetaData.class,
		IndexActionGroupMetaData.class, IndexPackage.class, IndexJobExecutionFactory.class, IndexJobExecutionMeta.class,
		JobPackage.class, JobExecutionMetaData.class, IndexActionRegisterServiceImpl.class, IndexingStrategy.class })
@Configuration
public class IndexConfig
{
	@Autowired
	private IndexActionRegisterService indexActionRegisterService;

	@Autowired
	private TransactionManager transactionManager;

	@Autowired
	private DataService dataService;

	@Autowired
	private IndexService indexService;

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
		return new IndexJobService(dataService, indexService, entityTypeFactory);
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
