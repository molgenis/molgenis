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

}
