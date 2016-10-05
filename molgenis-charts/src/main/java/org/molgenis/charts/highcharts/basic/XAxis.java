package org.molgenis.charts.highcharts.basic;

import java.util.List;

public class XAxis extends Axis
{
	private List<String> categories;

	public XAxis()
	{
	}

	public List<String> getCategories()
	{
		return categories;
	}

	public void setCategories(List<String> categories)
	{
		this.categories = categories;
	}
}
