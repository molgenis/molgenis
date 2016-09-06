package org.molgenis.data.elasticsearch.bootstrap;

import org.molgenis.data.elasticsearch.SearchService;
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

	@Autowired
	public IndexBootstrapper(MetaDataService metaDataService, SearchService searchService)
	{
		this.metaDataService = metaDataService;
		this.searchService = searchService;
	}

	public void bootstrap()
	{
		if (!searchService.hasMapping(AttributeMetaDataMetaData.ATTRIBUTE_META_DATA))
		{
			LOG.debug("No index for AttributeMetaData found, asuming missing index, (re)index all entities");
			metaDataService.getRepositories().forEach(repo -> searchService.rebuildIndex(repo));
			LOG.debug("Done (re)indexing all entities");
		}
		else
		{
			LOG.debug("Index for AttributeMetaData found, no (re)index needed");
		}
	}
}
