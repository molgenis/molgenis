package org.molgenis.data.elasticsearch;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.elasticsearch.search.SearchHit;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Queryable;

public class ElasticsearchEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private final Map<String, Object> source;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;

	public ElasticsearchEntity(String id, Map<String, Object> source, EntityMetaData entityMetaData,
			DataService dataService)
	{
		if (id == null) throw new IllegalArgumentException("id is null");
		if (source == null) throw new IllegalArgumentException("source is null");
		if (entityMetaData == null) throw new IllegalArgumentException("source is null");
		this.id = id;
		this.source = source;
		this.entityMetaData = entityMetaData;
		this.dataService = dataService;
	}

	public ElasticsearchEntity(SearchHit searchHit, EntityMetaData entityMetaData, DataService dataService)
	{
		if (searchHit == null) throw new IllegalArgumentException("searchHit is null");
		this.id = searchHit.getId();
		this.source = searchHit.getSource();
		this.entityMetaData = entityMetaData;
		this.dataService = dataService;
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
		AttributeMetaData attributeMetaData = entityMetaData.getAttribute(attributeName);
		Iterable<Entity> entities = new ArrayList<Entity>();
		if (attributeMetaData != null)
		{
			FieldType dataType = attributeMetaData.getDataType();
			if (dataType.getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.MREF))
			{
				if (attributeMetaData.getRefEntity() != null)
				{
					Iterable<Entity> iterable = dataService.getRepositoryByEntityName(attributeMetaData.getRefEntity()
							.getName().toLowerCase());

					QueryRule rule = new QueryRule(attributeMetaData.getRefEntity().getIdAttribute().getName(),
							QueryRule.Operator.EQUALS, get(attributeName + "."
									+ attributeMetaData.getRefEntity().getIdAttribute().getName().toLowerCase()));
					QueryImpl q = new QueryImpl();
					q.addRule(rule);
					entities = ((Queryable) iterable).findAll(q);
				}
				return entities;
			}
		}
		return source.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		source.put(attributeName, value);
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
