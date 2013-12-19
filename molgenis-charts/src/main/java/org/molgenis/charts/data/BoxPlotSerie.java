package org.molgenis.charts.data;

import java.util.ArrayList;
import java.util.List;

public class BoxPlotSerie
{
	private String name = "";
	private List<Double[]> data = new ArrayList<Double[]>();
	
	/**
	 * @return the data
	 */
	public List<Double[]> getData()
	{
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(List<Double[]> data)
	{
		this.data = data;
	}

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
}
