package org.molgenis.charts.data;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.charts.MolgenisSerieType;

/**
 * A list of XYData points to be used by XYDataCharts, represents a line/bar
 */
public class XYDataSerie extends MolgenisSerie
{
	private FieldTypeEnum attributeXFieldTypeEnum;
	private FieldTypeEnum attributeYFieldTypeEnum;
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
	public FieldTypeEnum getAttributeXFieldTypeEnum()
	{
		return attributeXFieldTypeEnum;
	}

	/**
	 * @param attributeXFieldTypeEnum the attributeXFieldTypeEnum to set
	 */
	public void setAttributeXFieldTypeEnum(FieldTypeEnum attributeXFieldTypeEnum)
	{
		this.attributeXFieldTypeEnum = attributeXFieldTypeEnum;
	}

	/**
	 * @return the attributeYFieldTypeEnum
	 */
	public FieldTypeEnum getAttributeYFieldTypeEnum()
	{
		return attributeYFieldTypeEnum;
	}

	/**
	 * @param attributeYFieldTypeEnum the attributeYFieldTypeEnum to set
	 */
	public void setAttributeYFieldTypeEnum(FieldTypeEnum attributeYFieldTypeEnum)
	{
		this.attributeYFieldTypeEnum = attributeYFieldTypeEnum;
	}
}
