package org.molgenis.data;

import java.util.List;
import java.util.Set;

public class AggregateResult
{
	private final List<List<Long>> matrix;
	private final Set<String> xLabels;
	private final Set<String> yLabels;

	public AggregateResult(List<List<Long>> matrix, Set<String> xLabels, Set<String> yLabels)
	{
		this.matrix = matrix;
		this.xLabels = xLabels;
		this.yLabels = yLabels;
	}

	public List<List<Long>> getMatrix()
	{
		return matrix;
	}

	public Set<String> getxLabels()
	{
		return xLabels;
	}

	public Set<String> getyLabels()
	{
		return yLabels;
	}

	@Override
	public String toString()
	{
		return "AggregateResult [matrix=" + matrix + ", xLabels=" + xLabels + ", yLabels=" + yLabels + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
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
