package org.molgenis.data;

import java.util.List;

public class AggregateResult
{
	private final List<List<Long>> matrix;
	private final List<String> xLabels;
	private final List<String> yLabels;
	private final Integer anonymizationThreshold;

	public AggregateResult(List<List<Long>> matrix, List<String> xLabels, List<String> yLabels,
			Integer anonymizationThreshold)
	{
		this.matrix = matrix;
		this.xLabels = xLabels;
		this.yLabels = yLabels;
		this.anonymizationThreshold = anonymizationThreshold;
	}

	public List<List<Long>> getMatrix()
	{
		return matrix;
	}

	public List<String> getxLabels()
	{
		return xLabels;
	}

	public List<String> getyLabels()
	{
		return yLabels;
	}

	public Integer getAnonymizationThreshold()
	{
		return anonymizationThreshold;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anonymizationThreshold == null) ? 0 : anonymizationThreshold.hashCode());
		result = prime * result + ((matrix == null) ? 0 : matrix.hashCode());
		result = prime * result + ((xLabels == null) ? 0 : xLabels.hashCode());
		result = prime * result + ((yLabels == null) ? 0 : yLabels.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AggregateResult other = (AggregateResult) obj;
		if (anonymizationThreshold == null)
		{
			if (other.anonymizationThreshold != null) return false;
		}
		else if (!anonymizationThreshold.equals(other.anonymizationThreshold)) return false;
		if (matrix == null)
		{
			if (other.matrix != null) return false;
		}
		else if (!matrix.equals(other.matrix)) return false;
		if (xLabels == null)
		{
			if (other.xLabels != null) return false;
		}
		else if (!xLabels.equals(other.xLabels)) return false;
		if (yLabels == null)
		{
			if (other.yLabels != null) return false;
		}
		else if (!yLabels.equals(other.yLabels)) return false;
		return true;
	}

}
