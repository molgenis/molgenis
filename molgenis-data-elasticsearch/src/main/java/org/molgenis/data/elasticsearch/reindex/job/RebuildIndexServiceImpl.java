package org.molgenis.data.elasticsearch.reindex.job;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.molgenis.security.user.MolgenisUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Schedules Reindex jobs.
 */
public class RebuildIndexServiceImpl implements RebuildIndexService
{
	private static final Logger LOG = LoggerFactory.getLogger(RebuildIndexServiceImpl.class);

	private final DataService dataService;
	private final ReindexJobFactory reindexJobFactory;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	@Autowired
	private MolgenisUserService molgenisUserService;

	public RebuildIndexServiceImpl(DataService dataService, ReindexJobFactory reindexJobFactory)
	{
		this.dataService = dataService;
		this.reindexJobFactory = reindexJobFactory;
	}

	@Override
	public void rebuildIndex(String transactionId)
	{
		LOG.debug("rebuildIndex {}", transactionId);
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
			LOG.warn("Skip reindex of transaction with id {}", transactionId);
		}

	}
}
