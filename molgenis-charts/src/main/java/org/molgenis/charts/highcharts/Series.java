package org.molgenis.charts.highcharts;

import java.util.List;

public class Series
{
	String name;
	List<Object> data;
	String type;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the data
	 */
	public List<Object> getData()
	{
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(List<Object> data)
	{
		this.data = data;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ChartType type)
	{
		this.type = type.toString();
	}
}
