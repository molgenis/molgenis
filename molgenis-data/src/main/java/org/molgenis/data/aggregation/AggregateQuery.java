package org.molgenis.data.aggregation;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;

public interface AggregateQuery
{
	Query<Entity> getQuery();

	Attribute getAttributeX();

	Attribute getAttributeY();

	Attribute getAttributeDistinct();
}
