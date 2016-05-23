package org.molgenis.data.elasticsearch.reindex.job;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.user.MolgenisUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import static java.time.OffsetDateTime.now;
import static java.util.Date.from;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.JobExecution.END_DATE;
import static org.molgenis.data.jobs.JobExecution.STATUS;
import static org.molgenis.data.jobs.JobExecution.Status.SUCCESS;
import static org.molgenis.data.reindex.job.ReindexJobExecutionMetaInterface.REINDEX_JOB_EXECUTION;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class RebuildIndexServiceImpl implements RebuildIndexService
{
	private static final Logger LOG = LoggerFactory.getLogger(RebuildIndexServiceImpl.class);

	private final DataService dataService;
	private final ReindexJobFactory reindexJobFactory;
	/**
	 * The {@link ReindexJob}s are executed on this thread.
	 */
	private final ExecutorService executorService;
	private final MolgenisUserService molgenisUserService;

	public RebuildIndexServiceImpl(DataService dataService, ReindexJobFactory reindexJobFactory,
			MolgenisUserService molgenisUserService, ExecutorService executorService)
	{
		this.dataService = requireNonNull(dataService);
		this.reindexJobFactory = requireNonNull(reindexJobFactory);
		this.molgenisUserService = requireNonNull(molgenisUserService);
		this.executorService = requireNonNull(executorService);
	}

	@Override
	@RunAsSystem
	public void rebuildIndex(String transactionId)
	{
		LOG.trace("Reindex transaction with id {}...", transactionId);
		Entity reindexActionJob = dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, transactionId);
		MolgenisUser admin = molgenisUserService.getUser("admin");

		if (reindexActionJob != null && admin != null)
		{
			ReindexJobExecution reindexJobExecution = new ReindexJobExecution(dataService);
			reindexJobExecution.setUser(admin);
			reindexJobExecution.setReindexActionJobID(transactionId);
			ReindexJob job = reindexJobFactory.createJob(reindexJobExecution);
			executorService.submit(job);
		}
		else
		{
			LOG.debug("Skipped reindex of transaction with id {}.", transactionId);
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
			Stream<Entity> executions = dataService.getRepository(REINDEX_JOB_EXECUTION).query()
					.lt(END_DATE, fiveMinutesAgo).and().eq(STATUS, SUCCESS.toString()).findAll();
			dataService.delete(REINDEX_JOB_EXECUTION, executions);
			LOG.debug("Cleaned up Reindex job executions.");
		});
	}
}
