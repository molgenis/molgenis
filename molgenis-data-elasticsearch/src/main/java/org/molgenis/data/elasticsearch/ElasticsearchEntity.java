package org.molgenis.data.elasticsearch;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.SearchHit;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractEntity;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class ElasticsearchEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private final Map<String, Object> source;
	private final EntityMetaData entityMetaData;

	public ElasticsearchEntity(String id, Map<String, Object> source, EntityMetaData entityMetaData)
	{
		if (id == null) throw new IllegalArgumentException("id is null");
		if (source == null) throw new IllegalArgumentException("source is null");
		if (entityMetaData == null) throw new IllegalArgumentException("source is null");
		this.id = id;
		this.source = source;
		this.entityMetaData = entityMetaData;
	}

	public ElasticsearchEntity(SearchHit searchHit, EntityMetaData entityMetaData)
	{
		if (searchHit == null) throw new IllegalArgumentException("searchHit is null");
		this.id = searchHit.getId();
		this.source = searchHit.getSource();
		this.entityMetaData = entityMetaData;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return Iterables.transform(getEntityMetaData().getAttributes(), new Function<AttributeMetaData, String>()
		{
			@Override
			public String apply(AttributeMetaData attributeMetaData)
			{
				return attributeMetaData.getName();
			}
		});
	}

	@Override
	public Object getIdValue()
	{
		return id;
	}

	@Override
	public String getLabelValue()
	{
		String labelAttributeName = entityMetaData.getLabelAttribute().getName();
		return source.get(labelAttributeName).toString();
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Collections.singletonList(entityMetaData.getLabelAttribute().getName());
	}

	@Override
	public Object get(String attributeName)
	{
		return source.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Entity values)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		throw new UnsupportedOperationException();
	}
}
