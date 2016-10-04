package org.molgenis.charts.highcharts.basic;

public enum AxisType
{
	LINEAR("linear"), LOGARITHMIC("logarithmic"), DATETIME("datetime"), CATEGORY("category");

	String type;

	AxisType(String type)
	{
		this.type = type;
	}

	public String toString()
	{
		return this.type;
	}
}
