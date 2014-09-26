package org.molgenis.data;

public interface AggregateQuery
{
	public Query getQuery();

	public AttributeMetaData getAttributeX();

	public AttributeMetaData getAttributeY();

	public AttributeMetaData getAttributeDistinct();

	/**
	 * Threshold value that when the count is smaller then this value, the value that is returned is -1.
	 * 
	 * 
	 * The user will be shown 'less then' for privacy reasons. So if threshold is 10 and the actual count is 9 count
	 * will be shown as <10
	 * 
	 * If null or 0 no threshold is defined.
	 */
	public Integer getAnonymizationThreshold();
}
