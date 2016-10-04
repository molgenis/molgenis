package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

public interface AggregateQuery
{
	public Query<Entity> getQuery();

	public Attribute getAttributeX();

	public Attribute getAttributeY();

	public Attribute getAttributeDistinct();
}
