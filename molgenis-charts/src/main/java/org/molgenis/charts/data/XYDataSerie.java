package org.molgenis.charts.data;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.charts.AbstractChart.MolgenisChartType;

/**
 * A list of XYData points to be used by XYDataCharts, represents a line/bar
 */
public class XYDataSerie
{
	private Class<?> attributeXJavaType;
	private Class<?> attributeYJavaType;
	private final List<XYData> data = new ArrayList<XYData>();
	private String name = "";
	private MolgenisChartType type;

	public void addData(XYData point)
	{
		data.add(point);
	}

	public List<XYData> getData()
	{
		return this.data;
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
	public MolgenisChartType getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(MolgenisChartType type)
	{
		this.type = type;
	}

	/**
	 * @return the attributeXJavaType
	 */
	public Class<?> getAttributeXJavaType()
	{
		return attributeXJavaType;
	}

	/**
	 * @param attributeXJavaType the attributeXJavaType to set
	 */
	public void setAttributeXJavaType(Class<?> attributeXJavaType)
	{
		this.attributeXJavaType = attributeXJavaType;
	}

	/**
	 * @return the attributeYJavaType
	 */
	public Class<?> getAttributeYJavaType()
	{
		return attributeYJavaType;
	}

	/**
	 * @param attributeYJavaType the attributeYJavaType to set
	 */
	public void setAttributeYJavaType(Class<?> attributeYJavaType)
	{
		this.attributeYJavaType = attributeYJavaType;
	}
	
	
}
