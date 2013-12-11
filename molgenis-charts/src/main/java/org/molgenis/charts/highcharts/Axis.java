package org.molgenis.charts.highcharts;

public class Axis
{
	AxisType type;
	Title title;

	public AxisType getType()
	{
		return type;
	}

	public void setType(AxisType type)
	{
		this.type = type;
	}
	
	/**
	 * @return the title
	 */
	public Title getTitle()
	{
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public Title setTitle(Title title)
	{
		this.title = title;
		return this.title;
	}
}
