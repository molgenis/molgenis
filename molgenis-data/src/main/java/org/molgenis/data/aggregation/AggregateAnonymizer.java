package org.molgenis.data.aggregation;

/**
 * Filter counts smaller the a threshold value
 *
 * @see AggregateQuery
 */
public interface AggregateAnonymizer
{
	long AGGREGATE_ANONYMIZATION_VALUE = -1;

	AnonymizedAggregateResult anonymize(AggregateResult aggregateResult, int threshold);
}
