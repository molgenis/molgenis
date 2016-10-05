package org.molgenis.data;

/**
 * Filter counts smaller the a threshold value
 *
 * @see org.molgenis.data.AggregateQuery
 */
public interface AggregateAnonymizer
{
	long AGGREGATE_ANONYMIZATION_VALUE = -1;

	AnonymizedAggregateResult anonymize(AggregateResult aggregateResult, int threshold);
}
