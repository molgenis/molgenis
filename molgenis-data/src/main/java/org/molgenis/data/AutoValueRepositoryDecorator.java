package org.molgenis.data;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.DATETIME;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.fieldtypes.StringField;

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
	public Integer add(Stream<? extends Entity> entities)
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
	public Stream<Entity> findAll(Query q)
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
	public Entity findOne(Object id, Fetch fetch)
	{
		return decoratedRepository.findOne(id, fetch);
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
	public void update(Stream<? extends Entity> entities)
	{
		decoratedRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Stream<Object> ids)
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
		autoAttrs.forEach(autoAttr -> {
			// set auto values unless a value already exists
			String autoAttrName = autoAttr.getName();
			if (entity.get(autoAttrName) == null)
			{
				if (autoAttr.equals(getEntityMetaData().getIdAttribute()))
				{
					entity.set(autoAttrName, idGenerator.generateId());
				}
				else if (autoAttr.getDataType().equals(DATE) || autoAttr.getDataType().equals(DATETIME))
				{
					entity.set(autoAttrName, new Date());
				}
				else
				{
					throw new RuntimeException("Invalid auto attribute: " + autoAttr.toString());
				}
			}
		});
		return entity;
	}
}
