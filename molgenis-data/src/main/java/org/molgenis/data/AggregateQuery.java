package org.molgenis.data;

import org.molgenis.data.meta.model.AttributeMetaData;

public interface AggregateQuery
{
	Query<Entity> getQuery();

	AttributeMetaData getAttributeX();

	AttributeMetaData getAttributeY();

	AttributeMetaData getAttributeDistinct();
}
