package org.molgenis.data;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;

/**
 * Adds auto id capabilities to a Repository
 */
public class AutoValueRepositoryDecorator implements Repository<Entity>
{
	private final Repository<Entity> decoratedRepository;
	private final IdGenerator idGenerator;

	public AutoValueRepositoryDecorator(Repository<Entity> decoratedRepository, IdGenerator idGenerator)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	public void add(Entity entity)
	{
		// auto date
		generateAutoDateOrDateTime(Arrays.asList(entity), getEntityMetaData().getAttributes());

		// auto id
		AttributeMetaData idAttr = getEntityMetaData().getIdAttribute();
		if (idAttr != null && idAttr.isAuto() && entity.getIdValue() == null && (idAttr.getDataType() == STRING))
		{
			entity.set(idAttr.getName(), idGenerator.generateId());
		}

		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		List<AttributeMetaData> autoAttrs = getAutoAttrs();
		if (!autoAttrs.isEmpty())
		{
			entities = entities.map(entity -> initAutoAttrs(entity, autoAttrs));
		}
		return decoratedRepository.add(entities);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decoratedRepository.forEachBatched(fetch, consumer, batchSize);
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
	public Set<QueryRule.Operator> getQueryOperators()
	{
		return decoratedRepository.getQueryOperators();
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
	public Query<Entity> query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		return decoratedRepository.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return decoratedRepository.findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepository.findAll(ids, fetch);
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
	public void update(Stream<Entity> entities)
	{
		decoratedRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decoratedRepository.deleteAll(ids);
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

	private void generateAutoDateOrDateTime(Iterable<? extends Entity> entities, Iterable<AttributeMetaData> attrs)
	{
		// get auto date and datetime attributes
		Iterable<AttributeMetaData> autoAttrs = Iterables.filter(attrs, new Predicate<AttributeMetaData>()
		{
			@Override
			public boolean apply(AttributeMetaData attr)
			{
				if (attr.isAuto())
				{
					AttributeType type = attr.getDataType();
					return type == DATE || type == DATE_TIME;
				}
				else
				{
					return false;
				}
			}
		});

		// set current date for auto date and datetime attributes
		Date dateNow = new Date();
		for (Entity entity : entities)
		{
			for (AttributeMetaData attr : autoAttrs)
			{
				AttributeType type = attr.getDataType();
				if (type == DATE)
				{
					entity.set(attr.getName(), dateNow);
				}
				else if (type == DATE_TIME)
				{
					entity.set(attr.getName(), dateNow);
				}
				else
				{
					throw new RuntimeException("Unexpected data type [" + type + "]");
				}

			}
		}
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepository.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepository.removeEntityListener(entityListener);
	}

	private List<AttributeMetaData> getAutoAttrs()
	{
		return StreamSupport.stream(getEntityMetaData().getAtomicAttributes().spliterator(), false)
				.filter(AttributeMetaData::isAuto).collect(Collectors.toList());
	}

	private Entity initAutoAttrs(Entity entity, List<AttributeMetaData> autoAttrs)
	{
		for (AttributeMetaData autoAttr : autoAttrs)
		{
			// set auto values unless a value already exists
			String autoAttrName = autoAttr.getName();
			if (entity.get(autoAttrName) == null)
			{
				if (autoAttrName.equals(getEntityMetaData().getIdAttribute().getName()))
				{
					entity.set(autoAttrName, idGenerator.generateId());
				}
				else if (autoAttr.getDataType() == DATE || autoAttr.getDataType() == DATE_TIME)
				{
					entity.set(autoAttrName, new Date());
				}
				else
				{
					throw new RuntimeException("Invalid auto attribute: " + autoAttr.toString());
				}
			}
		}
		return entity;
	}
}
