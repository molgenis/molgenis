package org.molgenis.data.elasticsearch.transaction;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RebuildIndexServiceImpl implements RebuildIndexService
{
	private static final Logger LOG = LoggerFactory.getLogger(RebuildIndexServiceImpl.class);

	private final DataService dataService;
	private final SearchService searchService;

	public RebuildIndexServiceImpl(DataService dataService, SearchService searchService)
	{
		this.dataService = requireNonNull(dataService);
		this.searchService = requireNonNull(searchService);
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		// LOG.info("transactionStarted [{}]", transactionId);
	}

	@Override
	public void commitTransaction(String transactionId)
	{
		LOG.info("commitTransaction [{}]", transactionId);
		this.rebuildIndex(transactionId);
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		// LOG.info("rollbackTransaction [{}]", transactionId);
	}

	@Override
	public void rebuildIndex(String transactionId)
	{
		new RebuildPartialIndex(transactionId, dataService, searchService).run();
	}
}
