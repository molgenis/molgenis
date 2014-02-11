package org.molgenis.charts.charttypes;

import org.molgenis.charts.AbstractChart;
import org.molgenis.charts.data.DataMatrix;

public class DataMatrixChart extends AbstractChart
{
	private final DataMatrix data;

	protected DataMatrixChart(MolgenisChartType type, DataMatrix data)
	{
		super.setType(type);
		if (data == null) throw new IllegalArgumentException("data is null");
		this.data = data;
	}

	public DataMatrix getData()
	{
		return data;
	}

}
