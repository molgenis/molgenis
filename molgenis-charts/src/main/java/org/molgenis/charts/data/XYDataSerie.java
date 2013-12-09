package org.molgenis.charts.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.charts.AbstractChart.AbstractChartType;

/**
 * A list of XYData points to be used by XYDataCharts, represents a line/bar
 */
public class XYDataSerie
{
	private final List<XYData> data = new ArrayList<XYData>();
	private String name = "";
	private AbstractChartType type;

	public void addData(XYData point)
	{
		data.add(point);
	}

	public List<XYData> getData()
	{
		return Collections.unmodifiableList(data);
	}

	@Override
	public String toString()
	{
		return "XYDataSerie [data=" + data + "]";
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return the type
	 */
	public AbstractChartType getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(AbstractChartType type)
	{
		this.type = type;
	}
}
