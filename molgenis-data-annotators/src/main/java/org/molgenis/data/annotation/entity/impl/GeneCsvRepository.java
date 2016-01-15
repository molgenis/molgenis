package org.molgenis.data.annotation.entity.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.AbstractRepository;

public class GeneCsvRepository extends AbstractRepository
{
	private final CsvRepository repository;
	private final Map<Object, Entity> index = new HashMap<Object, Entity>();
	private final String sourceAttributeName;
	private final String targetAttributeName;

	public GeneCsvRepository(File file, String sourceAttributeName, String targetAttributeName, char separator)
	{
		this.repository = new CsvRepository(file, null, separator);
		this.sourceAttributeName = sourceAttributeName;
		this.targetAttributeName = targetAttributeName;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return repository.getCapabilities();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return repository.getEntityMetaData();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return repository.iterator();
	}

	private Map<Object, Entity> getIndex()
	{
		if (index.isEmpty())
		{
			forEach(e -> {
				Object key = e.get(sourceAttributeName);
				if (key == null) throw new MolgenisDataException(
						"Missing value for attribute [" + sourceAttributeName + "] in entity [" + e + "]");
				index.put(key, e);
			});
		}

		return index;
	}
}
