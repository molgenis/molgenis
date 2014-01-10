package org.molgenis.charts.data;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.charts.MolgenisSerieType;
import org.molgenis.charts.highcharts.ChartType;

/**
 * A list of XYData points to be used by XYDataCharts, represents a line/bar
 */
public class XYDataSerie extends MolgenisSerie
{
	private Class<?> attributeXJavaType;
	private Class<?> attributeYJavaType;
	private List<XYData> data = new ArrayList<XYData>();
	
	public XYDataSerie()
	{
		this.setType(MolgenisSerieType.SCATTER);
	}

	/**
	 * @return the data
	 */
	public List<XYData> getData()
	{
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(List<XYData> data)
	{
		this.data = data;
	}
	
	/**
	 * @param xYData the xYData point to add
	 */
	public void addData(XYData xYData)
	{
		this.data.add(xYData);
	}
	
	/**
	 * @param xYData the xYData point to add
	 */
	public void addData(List<XYData> xYData)
	{
		this.data.addAll(xYData);
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
