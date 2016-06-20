package org.molgenis.data;

import org.molgenis.data.meta.AttributeMetaData;

public interface AggregateQuery
{
	public Query<Entity> getQuery();

	public AttributeMetaData getAttributeX();

	public AttributeMetaData getAttributeY();

	public AttributeMetaData getAttributeDistinct();
}
