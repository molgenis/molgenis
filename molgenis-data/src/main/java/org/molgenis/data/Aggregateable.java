package org.molgenis.data;

/**
 * Interface to be implemented by repositories that can produce aggregate results
 */
public interface Aggregateable
{
	/**
	 * 
	 * @param aggregateQuery
	 * @return
	 */
	AggregateResult aggregate(AggregateQuery aggregateQuery);
}
