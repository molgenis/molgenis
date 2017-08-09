package org.molgenis.charts.highcharts.convert;

import org.molgenis.charts.*;
import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.highcharts.basic.*;
import org.molgenis.charts.highcharts.chart.Chart;
import org.molgenis.charts.highcharts.stockchart.StockChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Arrays;

@Component
public class HighchartService extends AbstractChartVisualizationService
{

	@Autowired
	private HighchartSeriesUtil highchartSeriesUtil;

	public HighchartService()
	{
		super(Arrays.asList(MolgenisChartType.LINE_CHART, MolgenisChartType.SCATTER_CHART,
				MolgenisChartType.BOXPLOT_CHART));
	}

	@Override
	public Object renderChartInternal(AbstractChart chart, Model model)
	{
		if (MolgenisChartType.SCATTER_CHART.equals(chart.getType()))
		{
			return this.createScatterChart((XYDataChart) chart);
		}
		else if (MolgenisChartType.BOXPLOT_CHART.equals(chart.getType()))
		{
			return this.createBoxPlotChart((BoxPlotChart) chart);
		}
		return null;
	}

	/**
	 * Create a scatter plot
	 * <p>
	 * When the xAxisType equals MolgenisAxisType.DATETIME then the Highcharts Stockchart will be used to create a plot
	 *
	 * @param scatterChart
	 * @return Options
	 */
	public Options createScatterChart(XYDataChart scatterChart)
	{
		ChartConstructorType chartConstructorType;
		if (MolgenisAxisType.DATETIME.equals(scatterChart.getxAxisType()))
		{
			chartConstructorType = ChartConstructorType.STOCKCHART;
		}
		else
		{
			chartConstructorType = ChartConstructorType.CHART;
		}
		return createXYDataChart(scatterChart, chartConstructorType);
	}

	/**
	 * Create the Highcharts options from the given BoxPlotChart
	 *
	 * @param boxPlotChart
	 * @return Options
	 */
	protected Options createBoxPlotChart(BoxPlotChart boxPlotChart)
	{
		Options options = new Options();

		Chart chart = new Chart();
		chart.setType(ChartType.BOXPLOT).setWidth(boxPlotChart.getWidth()).setHeight(boxPlotChart.getHeight());

		XAxis xAxis = new XAxis();
		xAxis.setCategories(boxPlotChart.getCategories());
		xAxis.setTitle(new AxisTitle().setText(boxPlotChart.getxLabel()));

		YAxis yAxis = new YAxis();
		yAxis.setTitle(new AxisTitle().setText(boxPlotChart.getyLabel()));

		ChartTitle title = new ChartTitle().setText(boxPlotChart.getTitle()).setAlign(ChartAlign.CENTER);

		Legend legend = new Legend().setEnabled(true)
									.setAlign("center")
									.setLayout("horizontal")
									.setVerticalAlign("bottom");

		options.setChart(chart);
		options.setTitle(title);
		options.addxAxis(xAxis);
		options.addyAxis(yAxis);
		options.setCredits(new Credits());
		options.setLegend(legend);
		options.addSeries(highchartSeriesUtil.parseToBoxPlotSeriesList(boxPlotChart.getBoxPlotSeries()));
		options.addSeries(highchartSeriesUtil.parseToXYDataSeriesList(boxPlotChart.getxYDataSeries()));

		return options;
	}

	/**
	 * Create the Highcharts options from the given XYDataChart.
	 *
	 * @param xYDataChart
	 * @param chartConstructorType - When defining the chartConstructorType u can invloed the type of the Highchart constructor types
	 * @return Options
	 */
	protected Options createXYDataChart(XYDataChart xYDataChart, ChartConstructorType chartConstructorType)
	{
		Options options = new Options();

		final BasicChart chart;
		if (ChartConstructorType.CHART.equals(chartConstructorType))
		{
			chart = new Chart();
		}
		else
		{
			chart = new StockChart();
		}

		chart.setType(ChartType.getChartType(xYDataChart.getType()))
			 .setWidth(xYDataChart.getWidth())
			 .setHeight(xYDataChart.getHeight());

		XAxis xAxis = new XAxis();
		xAxis.setTitle(new AxisTitle().setText(xYDataChart.getxAxisLabel()).setAlign(AxisAlign.MIDDLE))
			 .setType(AxisType.valueOf(xYDataChart.getxAxisType().name()))
			 .setOrdinal(false);

		YAxis yAxis = new YAxis();
		yAxis.setTitle(new AxisTitle().setText(xYDataChart.getyAxisLabel()).setAlign(AxisAlign.MIDDLE))
			 .setType(AxisType.valueOf(xYDataChart.getyAxisType().name()));

		ChartTitle title = new ChartTitle().setText(xYDataChart.getTitle()).setAlign(ChartAlign.CENTER);

		Legend legend = new Legend().setEnabled(true)
									.setAlign("center")
									.setLayout("horizontal")
									.setVerticalAlign("bottom");

		options.setChart(chart);
		options.setTitle(title);
		options.addxAxis(xAxis);
		options.addyAxis(yAxis);
		options.setCredits(new Credits());
		options.setLegend(legend);
		options.setSeries(highchartSeriesUtil.parseToXYDataSeriesList(xYDataChart.getData()));

		return options;
	}
}
