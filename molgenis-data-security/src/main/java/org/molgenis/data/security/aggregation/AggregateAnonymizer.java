package org.molgenis.data.security.aggregation;

import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;

/**
 * Filter counts smaller the a threshold value
 *
 * @see AggregateQuery
 */
public interface AggregateAnonymizer {
  AnonymizedAggregateResult anonymize(AggregateResult aggregateResult, int threshold);
}
