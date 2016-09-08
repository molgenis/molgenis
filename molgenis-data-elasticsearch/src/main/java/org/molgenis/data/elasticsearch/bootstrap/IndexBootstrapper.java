package org.molgenis.data.elasticsearch.bootstrap;

import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexBootstrapper.class);

	private final MetaDataService metaDataService;

	private final SearchService searchService;
	private final IndexActionRegisterService indexActionRegisterService;

	@Autowired
	public IndexBootstrapper(MetaDataService metaDataService, SearchService searchService,
			IndexActionRegisterService indexActionRegisterService)
	{
		this.metaDataService = metaDataService;
		this.searchService = searchService;

		this.indexActionRegisterService = indexActionRegisterService;
	}

	public void bootstrap()
	{
		if (!searchService.hasMapping(AttributeMetaDataMetaData.ATTRIBUTE_META_DATA))
		{
			LOG.debug(
					"No index for AttributeMetaData found, asuming missing index, schedule (re)index for all entities");
			//one index job for all the repo's?
			metaDataService.getRepositories()
					.forEach(repo -> indexActionRegisterService.register(repo.getName(), null));
			LOG.debug("Done scheduling (re)index jobs for all entities");
		}
		else
		{
			LOG.debug("Index for AttributeMetaData found, index is present, no (re)index needed");
		}
	}
}
