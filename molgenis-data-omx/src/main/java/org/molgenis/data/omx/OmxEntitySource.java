package org.molgenis.data.omx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.search.SearchService;
import org.molgenis.security.runas.RunAsSystem;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * EntitySource implementation for omx (DataSet matrix)
 * 
 * There is only one OmxEntitySource instance in an application url is omx://
 */
public class OmxEntitySource implements EntitySource
{
	private final DataService dataService;
	private final SearchService searchService;

	public OmxEntitySource(DataService dataService, SearchService searchService)
	{
		this.dataService = dataService;
		this.searchService = searchService;
	}

	@Override
	@RunAsSystem
	//TODO JJ Find Iterable<String> a solution for collecting the data
	public Iterable<String> getEntityNames()
	{
		List<String> result = new ArrayList<String>();

		for (Entity e : dataService.findAll(DataSet.ENTITY_NAME))
		{
			result.add(e.getString(DataSet.IDENTIFIER));
		}
		for (Entity e : dataService.findAll(Protocol.ENTITY_NAME))
		{
			result.add(e.getString(Protocol.IDENTIFIER));
		}

		return result;
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		return new OmxRepository(dataService, searchService, name);
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	public String getUrl()
	{
		return "omx://";
	}

}
