package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

public interface AggregateQuery
{
	Query<Entity> getQuery();

	Attribute getAttributeX();

	Attribute getAttributeY();

	Attribute getAttributeDistinct();
}
