package org.molgenis.data;

import java.util.List;

/**
 * Filter counts smaller the a threshold value
 * 
 * @see also org.molgenis.data.AggregateQuery
 */
public interface AggregateAnonymizer
{
	public static final long AGGREGATE_ANONYMIZATION_VALUE = -1;

	List<List<Long>> anonymize(List<List<Long>> matrix, int threshold);
}
