package org.molgenis.omx.harmonization.plugin;

import org.molgenis.framework.db.Database;
import org.molgenis.search.SearchService;
import org.molgenis.util.DatabaseUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class AsyncOntologyAnnotator implements OntologyAnnotator, InitializingBean
{
	private SearchService searchService;

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	public void afterPropertiesSet() throws Exception
	{
		if (searchService == null) throw new IllegalArgumentException("Missing bean of type SearchService");
	}

	@Async
	public void annotate(Integer protocolId)
	{
		Database db = DatabaseUtil.createDatabase();

		try
		{

		}
		finally
		{
			DatabaseUtil.closeQuietly(db);
		}
	}

	@Override
	public float finishedPercentage()
	{
		return 0;
	}
}
