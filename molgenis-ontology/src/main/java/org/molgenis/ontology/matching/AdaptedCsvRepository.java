package org.molgenis.ontology.matching;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

public class AdaptedCsvRepository implements Repository
{
	private final CsvRepository csvRepository;
	private final String entityName;
	public static final String ALLOWED_IDENTIFIER = "Identifier";

	public AdaptedCsvRepository(String entityName, CsvRepository csvRepository)
	{
		this.csvRepository = csvRepository;
		this.entityName = entityName;
	}

	public EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData defaultEntityMetaData = new DefaultEntityMetaData(entityName);
		for (AttributeMetaData attributeMetaData : csvRepository.getEntityMetaData().getAttributes())
		{
			if (!ALLOWED_IDENTIFIER.equalsIgnoreCase(attributeMetaData.getName()))
			{
				defaultEntityMetaData.addAttributeMetaData(attributeMetaData);
			}
		}

		DefaultAttributeMetaData idAttribute = new DefaultAttributeMetaData(ALLOWED_IDENTIFIER);
		idAttribute.setIdAttribute(true);
		idAttribute.setNillable(false);
		defaultEntityMetaData.addAttributeMetaData(idAttribute);

		return defaultEntityMetaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final AtomicInteger count = new AtomicInteger(0);
		final Iterator<Entity> iterator = csvRepository.iterator();
		return new Iterator<Entity>()
		{
			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public Entity next()
			{
				Entity entity = iterator.next();
				if (entity.get(ALLOWED_IDENTIFIER) == null)
				{
					MapEntity mapEntity = new MapEntity();
					mapEntity.set(ALLOWED_IDENTIFIER, count.incrementAndGet());
					for (String attributeName : entity.getAttributeNames())
					{
						mapEntity.set(attributeName, entity.get(attributeName));
					}
					return mapEntity;
				}
				return entity;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void close() throws IOException
	{
		csvRepository.close();
	}

	@Override
	public String getName()
	{
		return entityName;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Query query()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(Query q)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity findOne(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity findOne(Object id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Object id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void add(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clearCache()
	{
		// TODO Auto-generated method stub

	}
}
