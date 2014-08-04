package org.molgenis.data.mongodb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;

import com.google.common.collect.Lists;

public class MongoEntity extends AbstractEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Map<String, Object> values = new CaseInsensitiveLinkedHashMap<Object>();
	private final EntityMetaData entityMetaData;
	private final MongoRepositoryCollection repositoryCollection;
	private List<String> attrNames;

	public MongoEntity(EntityMetaData entityMetaData, MongoRepositoryCollection repositoryCollection)
	{
		this.entityMetaData = entityMetaData;
		this.repositoryCollection = repositoryCollection;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		if (attrNames == null)
		{
			attrNames = Lists.newArrayList();
			for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
			{
				attrNames.add(attr.getName());
			}
		}

		return attrNames;
	}

	@Override
	public Object getIdValue()
	{
		return get(entityMetaData.getIdAttribute().getName());
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		AttributeMetaData labelAttr = entityMetaData.getLabelAttribute() != null ? entityMetaData.getLabelAttribute() : entityMetaData
				.getIdAttribute();

		return Arrays.asList(labelAttr.getName());
	}

	@Override
	public Object get(String attributeName)
	{
		Object value = values.get(attributeName);
		if (value == null)
		{
			AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
			if (attr == null)
			{
				throw new MolgenisDataException("Unknown attribute " + attributeName + " for entity "
						+ getEntityMetaData().getName());
			}

			value = attr.getDefaultValue();
		}

		return value;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
		{
			set(attr.getName(), entity.get(attr.getName()));
		}
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return getRefRepo(attributeName).findOne(get(attributeName));
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return getRefRepo(attributeName).findAll(getList(attributeName));
	}

	private MongoRepository getRefRepo(String attributeName)
	{
		AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
		if (attr == null)
		{
			throw new MolgenisDataException("Unknown attribute " + attributeName + " for entity "
					+ getEntityMetaData().getName());
		}

		EntityMetaData refEntity = attr.getRefEntity();
		if (refEntity == null)
		{
			throw new MolgenisDataException("RefEntity not defined for " + getEntityMetaData().getName() + "."
					+ attributeName);
		}

		MongoRepository refRepo = repositoryCollection.getRepositoryByEntityName(refEntity.getName());
		if (refRepo == null)
		{
			throw new MolgenisDataException("Missing repository  " + refEntity.getName());
		}

		return refRepo;
	}
}
