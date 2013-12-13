package org.molgenis.charts.highcharts;

public class Axis
{
	String type;
	AxisTitle title;

	public String getType()
	{
		return type;
	}

	public Axis setType(String type)
	{
		this.type = type;
		return this;
	}
	
	public Axis setType(AxisType type)
	{
		this.type = type.toString();
		return this;
	}
	
	public AxisTitle getTitle()
	{
		return title;
	}

	public Axis setTitle(AxisTitle title)
	{
		this.title = title;
		return this;
	}
}
