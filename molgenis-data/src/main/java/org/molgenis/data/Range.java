package org.molgenis.data;

/**
 * Long range, min and max included
 */
public class Range
{
	private final Long min;
	private final Long max;

	public Range(Long min, Long max)
	{
		this.min = min;
		this.max = max;
	}

	public Long getMin()
	{
		return min;
	}

	public Long getMax()
	{
		return max;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result + ((min == null) ? 0 : min.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Range other = (Range) obj;
		if (max == null)
		{
			if (other.max != null) return false;
		}
		else if (!max.equals(other.max)) return false;
		if (min == null)
		{
			if (other.min != null) return false;
		}
		else if (!min.equals(other.min)) return false;
		return true;
	}

}
