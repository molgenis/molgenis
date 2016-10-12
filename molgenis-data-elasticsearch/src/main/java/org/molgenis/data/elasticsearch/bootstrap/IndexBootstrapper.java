package org.molgenis.data.elasticsearch.bootstrap;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.index.job.IndexJobExecution;
import org.molgenis.data.elasticsearch.index.job.IndexJobExecutionMeta;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetaData;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.jobs.model.JobExecutionMetaData.*;

@Component
public class IndexBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexBootstrapper.class);

	private final MetaDataService metaDataService;

	private final SearchService searchService;
	private final IndexActionRegisterService indexActionRegisterService;
	private final DataService dataService;

	@Autowired
	public IndexBootstrapper(MetaDataService metaDataService, SearchService searchService,
			IndexActionRegisterService indexActionRegisterService, DataService dataService)
	{
		this.metaDataService = metaDataService;
		this.dataService = dataService;
		this.searchService = searchService;
		this.indexActionRegisterService = indexActionRegisterService;
	}

	public void bootstrap()
	{
		if (!searchService.hasMapping(AttributeMetaDataMetaData.ATTRIBUTE_META_DATA))
		{
			LOG.debug(
					"No index for Attribute found, asuming missing index, schedule (re)index for all entities");
			metaDataService.getRepositories()
					.forEach(repo -> indexActionRegisterService.register(repo.getName(), null));
			LOG.debug("Done scheduling (re)index jobs for all entities");
		}
		else
		{
			LOG.debug("Index for Attribute found, index is present, no (re)index needed");
			List<IndexJobExecution> failedIndexJobs = dataService.findAll(IndexJobExecutionMeta.INDEX_JOB_EXECUTION,
					new QueryImpl<IndexJobExecution>().eq(JobExecutionMetaData.STATUS, FAILED),
					IndexJobExecution.class).collect(Collectors.toList());
			failedIndexJobs.forEach(job -> registerNewIndexActionForDirtyJobs(job));
		}
	}

	private void registerNewIndexActionForDirtyJobs(IndexJobExecution indexJobExecution)
	{
		String id = indexJobExecution.getIndexActionJobID();
		dataService.findAll(IndexActionMetaData.INDEX_ACTION,
				new QueryImpl<IndexAction>().eq(IndexActionMetaData.INDEX_ACTION_GROUP_ATTR, id), IndexAction.class)
				.forEach(action -> indexActionRegisterService
						.register(action.getEntityFullName(), action.getEntityId()));
		dataService.delete(IndexJobExecutionMeta.INDEX_JOB_EXECUTION, indexJobExecution);
	}
}
