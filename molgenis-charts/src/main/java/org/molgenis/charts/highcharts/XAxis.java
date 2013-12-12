package org.molgenis.charts.highcharts;

import java.util.List;

public class XAxis extends Axis
{
	private List<String> categories;

	public List<String> getCategories()
	{
		return categories;
	}

	public void setCategories(List<String> categories)
	{
		this.categories = categories;
	}
}
