package org.molgenis.data.support;

import java.util.List;

import org.molgenis.data.AggregateAnonymizer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class AggregateAnonymizerImpl implements AggregateAnonymizer
{
	@Override
	public List<List<Long>> anonymize(List<List<Long>> matrix, final int threshold)
	{
		List<List<Long>> anonymizedmatrix = Lists.newArrayList();

		for (List<Long> row : matrix)
		{
			List<Long> anonymizedRow = Lists.transform(row, new Function<Long, Long>()
			{
				@Override
				public Long apply(Long input)
				{
					if (input == null) return null;
					return input < threshold ? AGGREGATE_ANONYMIZATION_VALUE : input;
				}

			});

			anonymizedmatrix.add(anonymizedRow);
		}

		return anonymizedmatrix;
	}
}
