package org.molgenis.charts;

import org.molgenis.charts.data.BoxPlotSerie;
import org.molgenis.charts.data.XYDataSerie;

import java.util.ArrayList;
import java.util.List;

/**
 * A Molgenis Box Plot Chart
 */
public class BoxPlotChart extends AbstractChart
{
	private List<XYDataSerie> xYDataSeries = new ArrayList<>();
	private List<BoxPlotSerie> boxPlotSeries = new ArrayList<>();
	private List<String> categories = new ArrayList<>();

	public BoxPlotChart()
	{
		this.setType(MolgenisChartType.BOXPLOT_CHART);
	}

	/**
	 * @return the categories
	 */
	public List<String> getCategories()
	{
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(List<String> categories)
	{
		this.categories = categories;
	}

	/**
	 * @return the xYDataSeries
	 */
	public List<XYDataSerie> getxYDataSeries()
	{
		return xYDataSeries;
	}

	/**
	 * @param xYDataSeries the xYDataSeries to set
	 */
	public void setxYDataSeries(List<XYDataSerie> xYDataSeries)
	{
		this.xYDataSeries = xYDataSeries;
	}

	/**
	 * @return the boxPlotSeries
	 */
	public List<BoxPlotSerie> getBoxPlotSeries()
	{
		return boxPlotSeries;
	}

	/**
	 * @param boxPlotSeries the boxPlotSeries to set
	 */
	public void setBoxPlotSeries(List<BoxPlotSerie> boxPlotSeries)
	{
		this.boxPlotSeries = boxPlotSeries;
	}

	/**
	 * @param boxPlotSerie the boxPlotSerie to add
	 */
	public void addBoxPlotSerie(BoxPlotSerie boxPlotSerie)
	{
		this.boxPlotSeries.add(boxPlotSerie);
	}

	/**
	 * @param xYDataSerie the xYDataSerie to add
	 */
	public void addXYDataSerie(XYDataSerie xYDataSerie)
	{
		this.xYDataSeries.add(xYDataSerie);
	}
}
