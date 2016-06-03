package org.molgenis.data.elasticsearch.reindex.job;

import static java.time.OffsetDateTime.now;
import static java.util.Date.from;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta.REINDEX_JOB_EXECUTION;
import static org.molgenis.data.jobs.JobExecution.Status.SUCCESS;
import static org.molgenis.data.jobs.JobExecutionMetaData.END_DATE;
import static org.molgenis.data.meta.system.ImportRunMetaData.STATUS;
import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.REINDEX_ACTION_JOB;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class ReindexServiceImpl implements ReindexService
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexServiceImpl.class);

	private final DataService dataService;
	private final ReindexJobFactory reindexJobFactory;
	private final ReindexJobExecutionFactory reindexJobExecutionFactory;
	/**
	 * The {@link ReindexJob}s are executed on this thread.
	 */
	private final ExecutorService executorService;

	public ReindexServiceImpl(DataService dataService, ReindexJobFactory reindexJobFactory,
			ReindexJobExecutionFactory reindexJobExecutionFactory, ExecutorService executorService)
	{
		this.dataService = requireNonNull(dataService);
		this.reindexJobFactory = requireNonNull(reindexJobFactory);
		this.reindexJobExecutionFactory = requireNonNull(reindexJobExecutionFactory);
		this.executorService = requireNonNull(executorService);
	}

	@Override
	@RunAsSystem
	public void rebuildIndex(String transactionId)
	{
		LOG.trace("Reindex transaction with id {}...", transactionId);
		Entity reindexActionJob = dataService.findOneById(REINDEX_ACTION_JOB, transactionId);

		if (reindexActionJob != null)
		{
			ReindexJobExecution reindexJobExecution = reindexJobExecutionFactory.create();
			reindexJobExecution.setUser("admin");
			reindexJobExecution.setReindexActionJobID(transactionId);
			ReindexJob job = reindexJobFactory.createJob(reindexJobExecution);
			executorService.submit(job);
		}
		else
		{
			LOG.debug("No reindex job found for id [{}].", transactionId);
		}
	}

	/**
	 * Cleans up succesful ReindexJobExecutions that finished longer than five minutes ago.
	 */
	@Scheduled(fixedRate = 5 * 60 * 1000)
	public void cleanupJobExecutions()
	{
		runAsSystem(() -> {
			LOG.trace("Clean up Reindex job executions...");
			Date fiveMinutesAgo = from(now().minusMinutes(5).toInstant());
			boolean reindexJobExecutionExists = dataService.hasRepository(REINDEX_JOB_EXECUTION);
			if (reindexJobExecutionExists)
			{
				Stream<Entity> executions = dataService.getRepository(REINDEX_JOB_EXECUTION).query()
						.lt(END_DATE, fiveMinutesAgo).and().eq(STATUS, SUCCESS.toString()).findAll();
				dataService.delete(REINDEX_JOB_EXECUTION, executions);
				LOG.debug("Cleaned up Reindex job executions.");
			}
			else
			{
				LOG.warn(REINDEX_JOB_EXECUTION + " does not exist");
			}
		});
	}

	@Override
	@RunAsSystem
	public boolean areAllIndiciesStable()
	{
		Long count = dataService.getRepository(REINDEX_ACTION).query()
				.in(ReindexActionMetaData.REINDEX_STATUS, Arrays.asList(
						// TODO implement mechanism to recover from failure.
						// ReindexActionMetaData.ReindexStatus.CANCELED.name(),
						// ReindexActionMetaData.ReindexStatus.FAILED.name(),
						ReindexActionMetaData.ReindexStatus.PENDING.name(),
						ReindexActionMetaData.ReindexStatus.STARTED.name())).count();
		return count == 0L;
	}

	@Override
	@RunAsSystem
	public boolean isIndexStableIncludingReferences(String entityName)
	{
		EntityMetaData emd = dataService.getEntityMetaData(entityName);
		Set<String> refEntityNames = StreamSupport.stream(emd.getAtomicAttributes().spliterator(), false)
				.map(AttributeMetaData::getRefEntity).filter(e -> e != null).map(EntityMetaData::getName)
				.collect(Collectors.toSet());
		refEntityNames.add(entityName);
		return refEntityNames.stream().allMatch(this::isIndexStable);
	}

	/**
	 * Check if the index for entity is stable
	 *
	 * @param entityName
	 * @return boolean
	 */
	private boolean isIndexStable(String entityName)
	{
		Long count = dataService.getRepository(REINDEX_ACTION).query()
				.eq(ReindexActionMetaData.ENTITY_FULL_NAME, entityName).and().in(ReindexActionMetaData.REINDEX_STATUS,
						Arrays.asList(ReindexActionMetaData.ReindexStatus.CANCELED.name(),
								ReindexActionMetaData.ReindexStatus.FAILED.name(),
								ReindexActionMetaData.ReindexStatus.PENDING.name(),
								ReindexActionMetaData.ReindexStatus.STARTED.name())).count();
		return count == 0L;
	}
}
