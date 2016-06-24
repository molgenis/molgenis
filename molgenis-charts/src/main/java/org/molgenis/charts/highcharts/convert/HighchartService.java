package org.molgenis.charts.highcharts.convert;

import java.util.Arrays;

import org.molgenis.charts.AbstractChart;
import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.AbstractChartVisualizationService;
import org.molgenis.charts.BoxPlotChart;
import org.molgenis.charts.MolgenisAxisType;
import org.molgenis.charts.XYDataChart;
import org.molgenis.charts.highcharts.basic.AxisAlign;
import org.molgenis.charts.highcharts.basic.AxisTitle;
import org.molgenis.charts.highcharts.basic.AxisType;
import org.molgenis.charts.highcharts.basic.BasicChart;
import org.molgenis.charts.highcharts.basic.ChartAlign;
import org.molgenis.charts.highcharts.basic.ChartConstructorType;
import org.molgenis.charts.highcharts.basic.ChartTitle;
import org.molgenis.charts.highcharts.basic.ChartType;
import org.molgenis.charts.highcharts.basic.Credits;
import org.molgenis.charts.highcharts.basic.Legend;
import org.molgenis.charts.highcharts.basic.Options;
import org.molgenis.charts.highcharts.basic.XAxis;
import org.molgenis.charts.highcharts.basic.YAxis;
import org.molgenis.charts.highcharts.chart.Chart;
import org.molgenis.charts.highcharts.stockchart.StockChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

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
	 * 
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
	 * @param model
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

		Legend legend = new Legend().setEnabled(true).setAlign("center").setLayout("horizontal")
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
	 * @param chartConstructorType
	 *            - When defining the chartConstructorType u can invloed the type of the Highchart constructor types
	 * @param model
	 *            - is not used
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

		chart.setType(ChartType.getChartType(xYDataChart.getType())).setWidth(xYDataChart.getWidth())
				.setHeight(xYDataChart.getHeight());

		XAxis xAxis = new XAxis();
		xAxis.setTitle(new AxisTitle().setText(xYDataChart.getxAxisLabel()).setAlign(AxisAlign.MIDDLE))
				.setType(AxisType.valueOf(xYDataChart.getxAxisType().name())).setOrdinal(false);

		YAxis yAxis = new YAxis();
		yAxis.setTitle(new AxisTitle().setText(xYDataChart.getyAxisLabel()).setAlign(AxisAlign.MIDDLE)).setType(
				AxisType.valueOf(xYDataChart.getyAxisType().name()));

		ChartTitle title = new ChartTitle().setText(xYDataChart.getTitle()).setAlign(ChartAlign.CENTER);

		Legend legend = new Legend().setEnabled(true).setAlign("center").setLayout("horizontal")
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
