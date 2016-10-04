package org.molgenis.charts.highcharts.basic;

public enum AxisAlign
{
	LOW("low"), MIDDLE("middle"), HIGH("high");

	private String align;

	AxisAlign(String align)
	{
		this.align = align;
	}

	public String toString()
	{
		return this.align;
	}
}
