package org.molgenis.data.omx;

import java.io.IOException;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;
import org.molgenis.omx.observ.DataSet;
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
	public Iterable<String> getEntityNames()
	{
		Iterable<Entity> dataSets = dataService.findAll(DataSet.ENTITY_NAME);

		return Iterables.transform(dataSets, new Function<Entity, String>()
		{
			@Override
			public String apply(Entity dataSet)
			{
				return dataSet.getString(DataSet.IDENTIFIER);
			}
		});
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
