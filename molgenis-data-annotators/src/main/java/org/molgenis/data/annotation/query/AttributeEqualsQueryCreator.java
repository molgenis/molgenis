package org.molgenis.data.annotation.query;

import static org.molgenis.data.support.QueryImpl.EQ;

import java.util.Arrays;
import java.util.Collection;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.entity.QueryCreator;

/**
 * Create a query that finds rows that match gene name
 */
public class AttributeEqualsQueryCreator implements QueryCreator
{
	private final AttributeMetaData attribute;

	public AttributeEqualsQueryCreator(AttributeMetaData attribute)
	{
		this.attribute = attribute;
	}

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(attribute);
	}

	@Override
	public Query createQuery(Entity entity)
	{
		Object value = entity.get(attribute.getName());
		return EQ(attribute.getName(), value);
	}

}
