package org.molgenis.charts.highcharts.basic;

public enum ChartAlign
{
	LEFT("left"), CENTER("center"), RIGHT("right");

	private String align;

	ChartAlign(String align)
	{
		this.align = align;
	}

	public String toString()
	{
		return this.align;
	}
}
