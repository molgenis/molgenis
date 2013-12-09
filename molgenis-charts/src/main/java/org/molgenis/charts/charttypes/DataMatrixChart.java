package org.molgenis.charts.charttypes;

import org.molgenis.charts.AbstractChart;
import org.molgenis.charts.data.DataMatrix;

public class DataMatrixChart extends AbstractChart
{
	private final DataMatrix data;

	protected DataMatrixChart(AbstractChartType type, DataMatrix data)
	{
		super(type);
		if (data == null) throw new IllegalArgumentException("data is null");
		this.data = data;
	}

	public DataMatrix getData()
	{
		return data;
	}

}
