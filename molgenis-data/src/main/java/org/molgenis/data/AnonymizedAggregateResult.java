package org.molgenis.data;

import java.util.List;

public class AnonymizedAggregateResult extends AggregateResult
{
	private int anonymizationThreshold;

	public AnonymizedAggregateResult(List<List<Long>> matrix, List<String> xLabels, List<String> yLabels,
			int anonymizationThreshold)
	{
		super(matrix, xLabels, yLabels);
		this.anonymizationThreshold = anonymizationThreshold;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + anonymizationThreshold;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		AnonymizedAggregateResult other = (AnonymizedAggregateResult) obj;
		if (anonymizationThreshold != other.anonymizationThreshold) return false;
		return true;
	}

}
