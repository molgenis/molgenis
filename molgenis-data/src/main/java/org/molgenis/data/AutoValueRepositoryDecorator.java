package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.util.HugeMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Adds auto id capabilities to a Repository
 */
public class AutoValueRepositoryDecorator implements Repository
{
	private final Repository decoratedRepository;
	private final IdGenerator idGenerator;

	public AutoValueRepositoryDecorator(Repository decoratedRepository, IdGenerator idGenerator)
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
		if ((idAttr != null) && idAttr.isAuto() && (idAttr.getDataType() instanceof StringField))
		{
			entity.set(idAttr.getName(), idGenerator.generateId());
		}

		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		// auto date
		generateAutoDateOrDateTime(entities, getEntityMetaData().getAttributes());

		// auto id
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
					FieldTypeEnum type = attr.getDataType().getEnumType();
					return type == FieldTypeEnum.DATE || type == FieldTypeEnum.DATE_TIME;
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
				FieldTypeEnum type = attr.getDataType().getEnumType();
				if (type == FieldTypeEnum.DATE)
				{
					entity.set(attr.getName(), dateNow);
				}
				else if (type == FieldTypeEnum.DATE_TIME)
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
	public void create()
	{
		decoratedRepository.create();
	}

	@Override
	public void drop()
	{
		decoratedRepository.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}
}
