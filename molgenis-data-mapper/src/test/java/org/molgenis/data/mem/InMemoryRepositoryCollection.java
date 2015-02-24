package org.molgenis.data.mem;

import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Repository;

import com.google.common.collect.Iterables;

public class InMemoryRepositoryCollection implements ManageableCrudRepositoryCollection
{
	private DataService dataService;

	public InMemoryRepositoryCollection(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return dataService.getEntityNames();
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		return dataService.getRepositoryByEntityName(name);
	}

	@Override
	public Iterator<CrudRepository> iterator()
	{
		return Iterables.<String, CrudRepository> transform(dataService.getEntityNames(),
				dataService::getCrudRepository).iterator();
	}

	@Override
	public CrudRepository add(EntityMetaData entityMetaData)
	{
		CrudRepository result = new InMemoryRepository(entityMetaData);
		dataService.addRepository(result);
		return result;
	}

	@Override
	public List<AttributeMetaData> update(EntityMetaData entityMetaData)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public void dropAttributeMetaData(String entityName, String attributeName)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public void dropEntityMetaData(String entityName)
	{
		dataService.removeRepository(entityName);
	}

}
