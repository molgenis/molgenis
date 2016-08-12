package org.molgenis.charts.highcharts.basic;

import java.util.List;

public class Series
{
	String name;
	List<Object> data;
	String type;
	Integer lineWidth;
	Marker marker;

	/**
	 * @return the marker
	 */
	public Marker getMarker()
	{
		return marker;
	}

	/**
	 * @param marker the marker to set
	 */
	public void setMarker(Marker marker)
	{
		this.marker = marker;
	}

	/**
	 * @return the lineWidth
	 */
	public Integer getLineWidth()
	{
		return lineWidth;
	}

	/**
	 * @param lineWidth the lineWidth to set
	 */
	public void setLineWidth(Integer lineWidth)
	{
		this.lineWidth = lineWidth;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
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
	 * @param data the data to set
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
	 * @param type the type to set
	 */
	public void setType(SeriesType type)
	{
		this.type = type.toString();
	}
}
