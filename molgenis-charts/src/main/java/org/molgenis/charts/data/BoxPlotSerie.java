package org.molgenis.charts.data;

import java.util.ArrayList;
import java.util.List;

public class BoxPlotSerie extends MolgenisSerie
{
	private List<Double[]> data = new ArrayList<>();

	/**
	 * @return the data
	 */
	public List<Double[]> getData()
	{
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(List<Double[]> data)
	{
		this.data = data;
	}

	/**
	 * @param data the data to add
	 */
	public void addData(Double[] data)
	{
		this.data.add(data);
	}
}
