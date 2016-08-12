package org.molgenis.charts.data;

import javax.annotation.Nullable;

/**
 * Data for charts that use xy point data like a linechart.
 */
public class XYData
{
	private final Object xvalue;
	private final Object yvalue;

	public XYData(@Nullable Object xvalue, @Nullable Object yvalue)
	{
		this.xvalue = xvalue;
		this.yvalue = yvalue;
	}

	public Object getXvalue()
	{
		return xvalue;
	}

	public Object getYvalue()
	{
		return yvalue;
	}

	@Override
	public String toString()
	{
		return "XYData [xvalue=" + xvalue + ", yvalue=" + yvalue + "]";
	}
}
