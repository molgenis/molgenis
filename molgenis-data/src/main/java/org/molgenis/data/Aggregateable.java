package org.molgenis.data;

/**
 * Interface to be implemented by repositories that can produce aggregate results
 */
public interface Aggregateable
{
	/**
	 * Creates counts off all possible combinations of xAttr and yAttr attributes
	 * 
	 * @param xAttr
	 * @param yAttr
	 * @return
	 */
	AggregateResult aggregate(AttributeMetaData xAttr, AttributeMetaData yAttr, Query q);
}
