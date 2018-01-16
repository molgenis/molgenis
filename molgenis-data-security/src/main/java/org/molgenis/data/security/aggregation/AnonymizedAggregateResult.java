package org.molgenis.data.security.aggregation;

import org.molgenis.data.aggregation.AggregateResult;

import java.util.List;

public class AnonymizedAggregateResult extends AggregateResult
{
	private final int anonymizationThreshold;

	public AnonymizedAggregateResult(List<List<Long>> matrix, List<Object> xLabels, List<Object> yLabels,
			int anonymizationThreshold)
	{
		super(matrix, xLabels, yLabels);
		this.anonymizationThreshold = anonymizationThreshold;
	}

	public int getAnonymizationThreshold()
	{
		return anonymizationThreshold;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getAnonymizationThreshold();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		AnonymizedAggregateResult other = (AnonymizedAggregateResult) obj;
		if (getAnonymizationThreshold() != other.getAnonymizationThreshold()) return false;
		return true;
	}
}
