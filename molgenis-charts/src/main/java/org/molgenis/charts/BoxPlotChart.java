package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.data.BoxPlotSerie;

/**
 * A Molgenis Box Plot Chart
 */
public class BoxPlotChart extends AbstractChart
{
	private List<BoxPlotSerie> series;

	public BoxPlotChart(List<BoxPlotSerie> series)
	{
		if (series == null) throw new IllegalArgumentException("data is null");
		this.series = series;
		this.setType(MolgenisChartType.BOXPLOT_CHART);
	}
	
	/**
	 * @return the boxPlotSeries
	 */
	public List<BoxPlotSerie> getSeries()
	{
		return series;
	}

	/**
	 * @param boxPlotSeries the boxPlotSeries to set
	 */
	public void setSeries(List<BoxPlotSerie> series)
	{
		this.series = series;
	}
}
