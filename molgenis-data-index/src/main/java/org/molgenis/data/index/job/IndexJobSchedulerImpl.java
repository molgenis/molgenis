package org.molgenis.data.index.job;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.jobs.JobExecutor;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.*;
import static org.molgenis.data.jobs.model.JobExecution.Status.SUCCESS;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.END_DATE;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.STATUS;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class IndexJobSchedulerImpl implements IndexJobScheduler
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexJobSchedulerImpl.class);

	private final DataService dataService;
	private final IndexJobExecutionFactory indexJobExecutionFactory;
	// the executor for the index jobs.
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final JobExecutor jobExecutor;
	private final IndexStatus indexStatus = new IndexStatus();

	public IndexJobSchedulerImpl(DataService dataService, IndexJobExecutionFactory indexJobExecutionFactory,
			JobExecutor jobExecutor)
	{
		this.dataService = requireNonNull(dataService);
		this.indexJobExecutionFactory = requireNonNull(indexJobExecutionFactory);
		this.jobExecutor = requireNonNull(jobExecutor);
	}

	@Override
	@RunAsSystem
	public void scheduleIndexJob(String transactionId)
	{
		LOG.trace("Index transaction with id {}...", transactionId);
		IndexActionGroup indexActionGroup = dataService.findOneById(INDEX_ACTION_GROUP, transactionId,
				IndexActionGroup.class);

		if (indexActionGroup != null)
		{
			Stream<Entity> indexActions = dataService.findAll(INDEX_ACTION,
					new QueryImpl<>().eq(INDEX_ACTION_GROUP_ATTR, indexActionGroup));
			Map<String, Long> numberOfActionsPerEntity = indexActions.collect(
					groupingBy(indexAction -> indexAction.getString(ENTITY_TYPE_ID), counting()));
			indexStatus.addActionCounts(numberOfActionsPerEntity);

			IndexJobExecution indexJobExecution = indexJobExecutionFactory.create();
			indexJobExecution.setUser("admin");
			indexJobExecution.setIndexActionJobID(transactionId);
			jobExecutor.submit(indexJobExecution, executorService)
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
	public void waitForIndexToBeStableIncludingReferences(EntityType entityType) throws InterruptedException
	{
		indexStatus.waitForIndexToBeStableIncludingReferences(entityType);
	}

	/**
	 * Cleans up successful IndexJobExecutions that finished longer than five minutes ago.
	 * delay for a minute to allow the transaction manager to become available
	 */
	@Scheduled(initialDelay = 1 * 60 * 1000, fixedRate = 5 * 60 * 1000)
	public void cleanupJobExecutions()
	{
		runAsSystem(() ->
		{
			LOG.trace("Clean up Index job executions...");
			Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
			boolean indexJobExecutionExists = dataService.hasRepository(IndexJobExecutionMeta.INDEX_JOB_EXECUTION);
			if (indexJobExecutionExists)
			{
				Stream<Entity> executions = dataService.getRepository(IndexJobExecutionMeta.INDEX_JOB_EXECUTION)
													   .query()
													   .lt(END_DATE, fiveMinutesAgo)
													   .and()
													   .eq(STATUS, SUCCESS.toString())
													   .findAll();
				dataService.delete(IndexJobExecutionMeta.INDEX_JOB_EXECUTION, executions);
				LOG.debug("Cleaned up Index job executions.");
			}
			else
			{
				LOG.warn(IndexJobExecutionMeta.INDEX_JOB_EXECUTION + " does not exist");
			}
		});
	}

}
