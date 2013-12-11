package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.data.XYDataSerie;

/**
 * A chart that uses xy data points like a LineChart or BarChart.
 */
public class XYDataChart extends AbstractChart
{
	private String xAxisLabel;
	private String xYxisLabel;
	private final List<XYDataSerie> data;

	protected XYDataChart(ChartType type, List<XYDataSerie> data, String xAxisLabel, String xYxisLabel)
	{
		super(type);
		if (data == null) throw new IllegalArgumentException("data is null");
		if (xAxisLabel == null) throw new IllegalArgumentException("xAxisLabel is null");
		if (xYxisLabel == null) throw new IllegalArgumentException("xYxisLabel is null");
		this.data = data;
		this.xAxisLabel = xAxisLabel;
		this.xYxisLabel = xYxisLabel;
	}

	/**
	 * @return the xAxisLabel
	 */
	public String getxAxisLabel()
	{
		return xAxisLabel;
	}

	/**
	 * @param xAxisLabel
	 *            the xAxisLabel to set
	 */
	public void setxAxisLabel(String xAxisLabel)
	{
		this.xAxisLabel = xAxisLabel;
	}

	/**
	 * @return the xYxisLabel
	 */
	public String getxYxisLabel()
	{
		return xYxisLabel;
	}

	/**
	 * @param xYxisLabel
	 *            the xYxisLabel to set
	 */
	public void setxYxisLabel(String xYxisLabel)
	{
		this.xYxisLabel = xYxisLabel;
	}

	/**
	 * @return the data
	 */
	public List<XYDataSerie> getData()
	{
		return data;
	}

}
