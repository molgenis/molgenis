package org.molgenis.data.security.aggregation;

import com.google.common.collect.Lists;
import org.molgenis.data.aggregation.AggregateResult;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.molgenis.data.security.aggregation.AggregateAnonymizer.AGGREGATE_ANONYMIZATION_VALUE;
import static org.testng.Assert.assertEquals;

public class AggregateAnonymizerImplTest
{
	@Test
	public void anonymize()
	{
		int threshold = 10;

		List<List<Long>> matrix = Lists.newArrayList();
		matrix.add(Arrays.asList(11L, 10L, 90L, 0L, 7L));
		matrix.add(Arrays.asList(null, 1L, 90L, 100L, 17L));
		matrix.add(Arrays.asList(11L, null, 90L, 5L, 10L));

		List<Object> xLabels = Arrays.asList("x1", "x2", "x3");
		List<Object> yLabels = Arrays.asList("y1", "y2", "y3");
		AnonymizedAggregateResult result = new AggregateAnonymizerImpl().anonymize(
				new AggregateResult(matrix, xLabels, yLabels), threshold);

		List<List<Long>> expectedMatrix = Lists.newArrayList();
		expectedMatrix.add(Arrays.asList(11L, AGGREGATE_ANONYMIZATION_VALUE, 90L, AGGREGATE_ANONYMIZATION_VALUE,
				AGGREGATE_ANONYMIZATION_VALUE));
		expectedMatrix.add(Arrays.asList(null, AGGREGATE_ANONYMIZATION_VALUE, 90L, 100L, 17L));
		expectedMatrix.add(Arrays.asList(11L, null, 90L, AGGREGATE_ANONYMIZATION_VALUE, AGGREGATE_ANONYMIZATION_VALUE));

		assertEquals(result, new AnonymizedAggregateResult(expectedMatrix, xLabels, yLabels, threshold));
	}
}
