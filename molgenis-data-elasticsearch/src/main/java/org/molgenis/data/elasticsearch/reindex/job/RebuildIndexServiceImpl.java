package org.molgenis.data.elasticsearch.reindex.job;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.user.MolgenisUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class RebuildIndexServiceImpl implements RebuildIndexService
{
	private static final Logger LOG = LoggerFactory.getLogger(RebuildIndexServiceImpl.class);

	private final DataService dataService;
	private final ReindexJobFactory reindexJobFactory;
	/**
	 * The {@link ReindexJob}s are executed on this thread.
	 */
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	@Autowired
	private MolgenisUserService molgenisUserService;

	public RebuildIndexServiceImpl(DataService dataService, ReindexJobFactory reindexJobFactory)
	{
		this.dataService = dataService;
		this.reindexJobFactory = reindexJobFactory;
	}

	@Override
	@RunAsSystem
	public void rebuildIndex(String transactionId)
	{
		LOG.trace("rebuildIndex {}", transactionId);
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
			LOG.debug("Skip reindex of transaction with id {}.", transactionId);
		}

	}
}
