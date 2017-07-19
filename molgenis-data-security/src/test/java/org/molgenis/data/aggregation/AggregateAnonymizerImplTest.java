package org.molgenis.data.aggregation;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.molgenis.data.aggregation.AggregateAnonymizer.AGGREGATE_ANONYMIZATION_VALUE;
import static org.testng.Assert.assertEquals;

public class AggregateAnonymizerImplTest
{
	@Test
	public void anonymize()
	{
		int threshold = 10;

		List<List<Long>> matrix = Lists.newArrayList();
		matrix.add(Arrays.asList(11l, 10l, 90l, 0l, 7l));
		matrix.add(Arrays.asList(null, 1l, 90l, 100l, 17l));
		matrix.add(Arrays.asList(11l, null, 90l, 5l, 10l));

		List<Object> xLabels = Arrays.asList("x1", "x2", "x3");
		List<Object> yLabels = Arrays.asList("y1", "y2", "y3");
		AnonymizedAggregateResult result = new AggregateAnonymizerImpl().anonymize(
				new AggregateResult(matrix, xLabels, yLabels), threshold);

		List<List<Long>> expectedMatrix = Lists.newArrayList();
		expectedMatrix.add(Arrays.asList(11l, AGGREGATE_ANONYMIZATION_VALUE, 90l, AGGREGATE_ANONYMIZATION_VALUE,
				AGGREGATE_ANONYMIZATION_VALUE));
		expectedMatrix.add(Arrays.asList(null, AGGREGATE_ANONYMIZATION_VALUE, 90l, 100l, 17l));
		expectedMatrix.add(Arrays.asList(11l, null, 90l, AGGREGATE_ANONYMIZATION_VALUE, AGGREGATE_ANONYMIZATION_VALUE));

		assertEquals(result, new AnonymizedAggregateResult(expectedMatrix, xLabels, yLabels, threshold));
	}
}
