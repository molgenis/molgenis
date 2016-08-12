package org.molgenis.charts.highcharts.basic;

public class Axis
{
	private String type;
	private AxisTitle title;
	private Boolean ordinal;

	/**
	 * @return the ordinal
	 */
	public Boolean getOrdinal()
	{
		return ordinal;
	}

	/**
	 * Works only on Highstock charts
	 *
	 * @param ordinal the ordinal to set
	 */
	public Axis setOrdinal(Boolean ordinal)
	{
		this.ordinal = ordinal;
		return this;
	}

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
