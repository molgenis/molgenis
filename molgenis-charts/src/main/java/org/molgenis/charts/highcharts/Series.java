package org.molgenis.charts.highcharts;

import java.util.List;

public class Series <T>
{
	List<T> data;

	public List<T> getData()
	{
		return (List<T>) data;
	}

	public void setData(List<T> data)
	{
		this.data = (List<T>) data;
	}
	
}
