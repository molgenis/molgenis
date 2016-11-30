package org.molgenis.charts.data;

import org.molgenis.charts.MolgenisSerieType;
import org.molgenis.data.meta.AttributeType;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of XYData points to be used by XYDataCharts, represents a line/bar
 */
public class XYDataSerie extends MolgenisSerie
{
	private AttributeType attributeXFieldTypeEnum;
	private AttributeType attributeYFieldTypeEnum;
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
	 * @return the attributeXFieldTypeEnum
	 */
	public AttributeType getAttributeXFieldTypeEnum()
	{
		return attributeXFieldTypeEnum;
	}

	/**
	 * @param attributeXFieldTypeEnum the attributeXFieldTypeEnum to set
	 */
	public void setAttributeXFieldTypeEnum(AttributeType attributeXFieldTypeEnum)
	{
		this.attributeXFieldTypeEnum = attributeXFieldTypeEnum;
	}

	/**
	 * @return the attributeYFieldTypeEnum
	 */
	public AttributeType getAttributeYFieldTypeEnum()
	{
		return attributeYFieldTypeEnum;
	}

	/**
	 * @param attributeYFieldTypeEnum the attributeYFieldTypeEnum to set
	 */
	public void setAttributeYFieldTypeEnum(AttributeType attributeYFieldTypeEnum)
	{
		this.attributeYFieldTypeEnum = attributeYFieldTypeEnum;
	}
}
