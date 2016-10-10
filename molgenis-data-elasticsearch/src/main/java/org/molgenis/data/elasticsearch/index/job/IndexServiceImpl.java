package org.molgenis.data.elasticsearch.index.job;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static java.time.OffsetDateTime.now;
import static java.util.Date.from;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.molgenis.data.elasticsearch.index.job.IndexJobExecutionMeta.INDEX_JOB_EXECUTION;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.*;
import static org.molgenis.data.jobs.model.JobExecution.Status.SUCCESS;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.END_DATE;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.STATUS;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class IndexServiceImpl implements IndexService
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexServiceImpl.class);

	private final DataService dataService;
	private final IndexJobFactory indexJobFactory;
	private final IndexJobExecutionFactory indexJobExecutionFactory;

	/**
	 * The {@link IndexJob}s are executed on this thread.
	 */
	private final ExecutorService executorService;
	private final IndexStatus indexStatus = new IndexStatus();

	public IndexServiceImpl(DataService dataService, IndexJobFactory indexJobFactory,
			IndexJobExecutionFactory indexJobExecutionFactory, ExecutorService executorService)
	{
		this.dataService = requireNonNull(dataService);
		this.indexJobFactory = requireNonNull(indexJobFactory);
		this.indexJobExecutionFactory = requireNonNull(indexJobExecutionFactory);
		this.executorService = requireNonNull(executorService);
	}

	@Override
	@RunAsSystem
	public void rebuildIndex(String transactionId)
	{
		LOG.trace("Index transaction with id {}...", transactionId);
		IndexActionGroup indexActionGroup = dataService
				.findOneById(INDEX_ACTION_GROUP, transactionId, IndexActionGroup.class);

		if (indexActionGroup != null)
		{
			Stream<Entity> indexActions = dataService
					.findAll(INDEX_ACTION, new QueryImpl<>().eq(INDEX_ACTION_GROUP_ATTR, indexActionGroup));
			Map<String, Long> numberOfActionsPerEntity = indexActions
					.collect(groupingBy(indexAction -> indexAction.getString(ENTITY_FULL_NAME), counting()));
			indexStatus.addActionCounts(numberOfActionsPerEntity);

			IndexJobExecution indexJobExecution = indexJobExecutionFactory.create();
			indexJobExecution.setUser("admin");
			indexJobExecution.setIndexActionJobID(transactionId);
			IndexJob job = indexJobFactory.createJob(indexJobExecution);
			CompletableFuture.runAsync(job::call, executorService)
					.whenComplete((a, b) -> indexStatus.removeActionCounts(numberOfActionsPerEntity));
		}
		else
		{
			LOG.debug("No index job found for id [{}].", transactionId);
		}
	}

	@Override
	@RunAsSystem
	public void waitForAllIndicesStable() throws InterruptedException
	{
		indexStatus.waitForAllEntitiesToBeStable();
	}

	@Override
	@RunAsSystem
	public void waitForIndexToBeStableIncludingReferences(String entityName) throws InterruptedException
	{
		indexStatus.waitForIndexToBeStableIncludingReferences(dataService.getEntityType(entityName));
	}

	/**
	 * Cleans up successful IndexJobExecutions that finished longer than five minutes ago.
	 */
	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void cleanupJobExecutions()
	{
		runAsSystem(() ->
		{
			LOG.trace("Clean up Index job executions...");
			Date fiveMinutesAgo = from(now().minusMinutes(5).toInstant());
			boolean indexJobExecutionExists = dataService.hasRepository(INDEX_JOB_EXECUTION);
			if (indexJobExecutionExists)
			{
				Stream<Entity> executions = dataService.getRepository(INDEX_JOB_EXECUTION).query()
						.lt(END_DATE, fiveMinutesAgo).and().eq(STATUS, SUCCESS.toString()).findAll();
				dataService.delete(INDEX_JOB_EXECUTION, executions);
				LOG.debug("Cleaned up Index job executions.");
			}
			else
			{
				LOG.warn(INDEX_JOB_EXECUTION + " does not exist");
			}
		});
	}

}
