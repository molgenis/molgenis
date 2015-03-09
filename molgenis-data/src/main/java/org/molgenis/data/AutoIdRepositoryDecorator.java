package org.molgenis.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.util.HugeMap;

/**
 * Adds auto id capabilities to a Repository
 */
public class AutoIdRepositoryDecorator implements Repository
{
	private final Repository decoratedRepository;
	private final IdGenerator idGenerator;

	public AutoIdRepositoryDecorator(Repository decoratedRepository, IdGenerator idGenerator)
	{
		this.decoratedRepository = decoratedRepository;
		this.idGenerator = idGenerator;
	}

	@Override
	public void add(Entity entity)
	{
		AttributeMetaData attr = getEntityMetaData().getIdAttribute();
		if ((attr != null) && attr.isAuto() && (attr.getDataType() instanceof StringField))
		{
			entity.set(attr.getName(), idGenerator.generateId());
		}

		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		AttributeMetaData attr = getEntityMetaData().getIdAttribute();
		if ((attr != null) && attr.isAuto() && (attr.getDataType() instanceof StringField))
		{
			HugeMap<Integer, Object> idMap = new HugeMap<>();
			try
			{
				Iterable<? extends Entity> decoratedEntities = new AutoIdEntityIterableDecorator(getEntityMetaData(),
						entities, idGenerator, idMap);
				return decoratedRepository.add(decoratedEntities);
			}
			finally
			{
				IOUtils.closeQuietly(idMap);
			}
		}

		return decoratedRepository.add(entities);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public Query query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		decoratedRepository.update(records);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public void flush()
	{
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

}
