package org.molgenis.omx.decorators;

import java.util.List;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.util.ApplicationContextProvider;

/**
 * Reregister omx entitysource in the DataService if a DataSet is added or deleted
 */
public class DataSetRepositoryDecorator extends CrudRepositoryDecorator
{

	public DataSetRepositoryDecorator(CrudRepository decoratedRepository)
	{
		super(decoratedRepository);
	}

	@Override
	public Integer add(Entity entity)
	{
		Integer result = super.add(entity);
		reregisterOmxEntitySource();

		return result;
	}

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		super.add(entities);
		reregisterOmxEntitySource();
	}

	@Override
	public void delete(Entity entity)
	{
		super.delete(entity);
		reregisterOmxEntitySource();
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		super.delete(entities);
		reregisterOmxEntitySource();
	}

	@Override
	public void deleteAll()
	{
		super.deleteAll();
		reregisterOmxEntitySource();
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		super.update(entities, dbAction, keyName);
		reregisterOmxEntitySource();
	}

	private void reregisterOmxEntitySource()
	{
		DataService dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);
		if (dataService != null)
		{
			dataService.registerEntitySource("omx://");
		}
	}

}
