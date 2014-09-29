package org.molgenis.data;

public interface AggregateQuery
{
	public Query getQuery();

	public AttributeMetaData getAttributeX();

	public AttributeMetaData getAttributeY();

	public AttributeMetaData getAttributeDistinct();
}
