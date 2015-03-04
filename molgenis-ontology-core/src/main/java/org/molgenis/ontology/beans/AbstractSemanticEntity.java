package org.molgenis.ontology.beans;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.AbstractEntity;

public abstract class AbstractSemanticEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;
	protected final EntityMetaData entityMetaData;
	protected final SearchService searchService;
	protected final Entity entity;

	public AbstractSemanticEntity(Entity entity, EntityMetaData entityMetaData, SearchService searchService)
	{
		this.entityMetaData = entityMetaData;
		this.searchService = searchService;
		this.entity = entity;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public void set(Entity values)
	{
		entity.set(values);
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		List<String> attributeNames = new ArrayList<String>();
		for (AttributeMetaData attribute : entityMetaData.getAttributes())
		{
			attributeNames.add(attribute.getName());
		}
		return attributeNames;
	}

	@Override
	public Object getIdValue()
	{
		return entity.getIdValue();
	}

	@Override
	public void set(String attributeName, Object value)
	{
		throw new UnsupportedOperationException();
	}

	protected String getValueInternal(String attributeName)
	{
		if (!StringUtils.isEmpty(entity.getString(attributeName)))
		{
			return entity.getString(attributeName);
		}
		return StringUtils.EMPTY;
	}

	public Entity getEntity()
	{
		return entity;
	}
}