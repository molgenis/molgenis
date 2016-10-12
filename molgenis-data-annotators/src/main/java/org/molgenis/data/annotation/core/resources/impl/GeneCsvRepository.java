package org.molgenis.data.annotation.core.resources.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.AbstractRepository;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class GeneCsvRepository extends AbstractRepository
{
	private final CsvRepository repository;
	private final Map<Object, Entity> index = new HashMap<Object, Entity>();
	private final String sourceAttributeName;
	private final String targetAttributeName;

	public GeneCsvRepository(File file, String sourceAttributeName, String targetAttributeName,
			EntityMetaDataFactory entityMetaFactory, AttributeFactory attrMetaFactory, char separator)
	{
		this.repository = new CsvRepository(file, entityMetaFactory, attrMetaFactory, null, separator);
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

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (q.getRules().isEmpty()) return getIndex().values().stream();
		if ((q.getRules().size() != 1) || (q.getRules().get(0).getOperator() != Operator.EQUALS) || !targetAttributeName
				.equals(q.getRules().get(0).getField()))
		{
			throw new MolgenisDataException(
					"The only query allowed on this Repository is '" + targetAttributeName + " EQUALS'");
		}

		Entity result = getIndex().get(q.getRules().get(0).getValue());
		return result == null ? Stream.empty() : Stream.of(result);
	}

	private Map<Object, Entity> getIndex()
	{
		if (index.isEmpty())
		{
			forEach(e ->
			{
				Object key = e.get(sourceAttributeName);
				if (key == null) throw new MolgenisDataException(
						"Missing value for attribute [" + sourceAttributeName + "] in entity [" + e + "]");
				index.put(key, e);
			});
		}

		return index;
	}
}
