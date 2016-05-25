package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RowLevelSecurityRepositoryDecorator implements Repository
{
	public static final String UPDATE_ATTRIBUTE = "_UPDATE";
	public static final String READ_ATTRIBUTE = "_READ";
	public static final String DELETE_ATTRIBUTE = "_DELETE";
	public static final String MANAGE_ATTRIBUTE = "_MANAGE";
	public static final List<String> ROW_LEVEL_SECURITY_ATTRIBUTES = Arrays.asList(UPDATE_ATTRIBUTE, READ_ATTRIBUTE,
			DELETE_ATTRIBUTE, MANAGE_ATTRIBUTE);

	private final Repository decoratedRepository;

	public RowLevelSecurityRepositoryDecorator(Repository decoratedRepository)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
	}

	public Stream<Entity> stream(Fetch fetch)
	{
		return decoratedRepository.stream(fetch);
	}

	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	public String getName()
	{
		return decoratedRepository.getName();
	}

	public EntityMetaData getEntityMetaData()
	{
		if (isRowLevelSecured())
		{
			return new RowLevelSecurityEntityMetaData(decoratedRepository.getEntityMetaData());
		}
		else
		{
			return decoratedRepository.getEntityMetaData();
		}
	}

	public long count()
	{
		return decoratedRepository.count();
	}

	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	public Query query()
	{
		return decoratedRepository.query();
	}

	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	public Stream<Entity> findAll(Query q)
	{
		return decoratedRepository.findAll(q);
	}

	public Entity findOne(Query q)
	{
		return decoratedRepository.findOne(q);
	}

	public Entity findOne(Object id)
	{
		return decoratedRepository.findOne(id);
	}

	public Entity findOne(Object id, Fetch fetch)
	{
		return decoratedRepository.findOne(id, fetch);
	}

	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepository.findAll(ids, fetch);
	}

	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	public void update(Entity entity)
	{
		decoratedRepository.update(entity);
	}

	public void update(Stream<? extends Entity> entities)
	{
		decoratedRepository.update(entities);
	}

	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	public void delete(Stream<? extends Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	public void deleteById(Stream<Object> ids)
	{
		decoratedRepository.deleteById(ids);
	}

	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
	}

	public Integer add(Stream<? extends Entity> entities)
	{
		return decoratedRepository.add(entities);
	}

	public void flush()
	{
		decoratedRepository.flush();
	}

	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

	public void create()
	{
		decoratedRepository.create();
	}

	public void drop()
	{
		decoratedRepository.drop();
	}

	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}

	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepository.addEntityListener(entityListener);
	}

	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepository.removeEntityListener(entityListener);
	}

	private boolean isRowLevelSecured()
	{
		return decoratedRepository.getEntityMetaData().isRowLevelSecured();
	}

	private class RowLevelSecurityEntityMetaData implements EntityMetaData
	{
		private EntityMetaData entityMetaData;

		public RowLevelSecurityEntityMetaData(EntityMetaData entityMetaData)
		{
			this.entityMetaData = entityMetaData;
		}

		public Package getPackage()
		{
			return entityMetaData.getPackage();
		}

		public String getName()
		{
			return entityMetaData.getName();
		}

		public String getSimpleName()
		{
			return entityMetaData.getSimpleName();
		}

		public String getBackend()
		{
			return entityMetaData.getBackend();
		}

		public boolean isAbstract()
		{
			return entityMetaData.isAbstract();
		}

		public String getLabel()
		{
			return entityMetaData.getLabel();
		}

		public String getLabel(String languageCode)
		{
			return entityMetaData.getLabel(languageCode);
		}

		public Set<String> getLabelLanguageCodes()
		{
			return entityMetaData.getLabelLanguageCodes();
		}

		public String getDescription()
		{
			return entityMetaData.getDescription();
		}

		public String getDescription(String languageCode)
		{
			return entityMetaData.getDescription(languageCode);
		}

		public Set<String> getDescriptionLanguageCodes()
		{
			return entityMetaData.getDescriptionLanguageCodes();
		}

		public Iterable<AttributeMetaData> getAttributes()
		{
			return StreamSupport.stream(entityMetaData.getAttributes().spliterator(), false).filter(attribute -> {
				if (ROW_LEVEL_SECURITY_ATTRIBUTES.contains(attribute.getName()))
				{
					return false;
				}
				else
				{
					return true;
				}
			}).collect(Collectors.toList());
		}

		public Iterable<AttributeMetaData> getOwnAttributes()
		{
			return entityMetaData.getOwnAttributes();
		}

		public Iterable<AttributeMetaData> getAtomicAttributes()
		{
			return StreamSupport.stream(entityMetaData.getAtomicAttributes().spliterator(), false).filter(attribute -> {
				if (ROW_LEVEL_SECURITY_ATTRIBUTES.contains(attribute.getName()))
				{
					return false;
				}
				else
				{
					return true;
				}
			}).collect(Collectors.toList());
		}

		public Iterable<AttributeMetaData> getOwnAtomicAttributes()
		{
			return entityMetaData.getOwnAtomicAttributes();
		}

		public AttributeMetaData getIdAttribute()
		{
			return entityMetaData.getIdAttribute();
		}

		public AttributeMetaData getOwnIdAttribute()
		{
			return entityMetaData.getOwnIdAttribute();
		}

		public AttributeMetaData getLabelAttribute()
		{
			return entityMetaData.getLabelAttribute();
		}

		public AttributeMetaData getOwnLabelAttribute()
		{
			return entityMetaData.getOwnLabelAttribute();
		}

		public AttributeMetaData getLabelAttribute(String languageCode)
		{
			return entityMetaData.getLabelAttribute(languageCode);
		}

		public Iterable<AttributeMetaData> getLookupAttributes()
		{
			return entityMetaData.getLookupAttributes();
		}

		public Iterable<AttributeMetaData> getOwnLookupAttributes()
		{
			return entityMetaData.getOwnLookupAttributes();
		}

		public AttributeMetaData getLookupAttribute(String attributeName)
		{
			return entityMetaData.getLookupAttribute(attributeName);
		}

		public AttributeMetaData getAttribute(String attributeName)
		{
			return entityMetaData.getAttribute(attributeName);
		}

		public boolean hasAttributeWithExpression()
		{
			return entityMetaData.hasAttributeWithExpression();
		}

		public EntityMetaData getExtends()
		{
			return entityMetaData.getExtends();
		}

		public Class<? extends Entity> getEntityClass()
		{
			return entityMetaData.getEntityClass();
		}

		public boolean isRowLevelSecured()
		{
			return entityMetaData.isRowLevelSecured();
		}
	}
}
