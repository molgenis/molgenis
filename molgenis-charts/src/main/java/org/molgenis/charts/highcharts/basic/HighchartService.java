package org.molgenis.charts.highcharts.basic;

import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.persistence.jpa.jpql.parser.DateTime;
import org.molgenis.charts.AbstractChart;
import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.AbstractChartVisualizationService;
import org.molgenis.charts.BoxPlotChart;
import org.molgenis.charts.MolgenisAxisType;
import org.molgenis.charts.XYDataChart;
import org.molgenis.charts.highcharts.chart.Chart;
import org.molgenis.charts.highcharts.data.HighchartsDataUtil;
import org.molgenis.charts.highcharts.stockchart.StockChart;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class HighchartService extends AbstractChartVisualizationService
{
	private static final Logger logger = Logger.getLogger(HighchartService.class);

	public HighchartService()
	{
		super(Arrays.asList(MolgenisChartType.LINE_CHART, MolgenisChartType.SCATTER_CHART,
				MolgenisChartType.BOXPLOT_CHART));
	}

	@Override
	protected Object renderChartInternal(AbstractChart chart, Model model)
	{
		if (MolgenisChartType.SCATTER_CHART.equals(chart.getType()))
		{
			return this.createScatterChart((XYDataChart) chart, model);
		}
		else if (MolgenisChartType.BOXPLOT_CHART.equals(chart.getType()))
		{
			return this.createBoxPlotChart((BoxPlotChart) chart, model);
		}
		return null;
	}
	
	public Options createScatterChart(XYDataChart scatterChart, Model model)
	{
		ChartConstructorType chartConstructorType;
		if(MolgenisAxisType.DATETIME.equals(scatterChart.getxAxisType())) {
			chartConstructorType = ChartConstructorType.STOCKCHART;
		} else {
			chartConstructorType = ChartConstructorType.CHART;
		}
		return createXYDataChart(scatterChart, chartConstructorType, model);
	}
	
	protected Options createBoxPlotChart(BoxPlotChart boxPlotChart, Model model)
	{
		Options options = new Options();
		
		Chart chart = new Chart();
		chart.setType(ChartType.BOXPLOT)
			.setWidth(boxPlotChart.getWidth())
			.setHeight(boxPlotChart.getHeight());
		
		XAxis xAxis = new XAxis();
		xAxis.setCategories(boxPlotChart.getCategories());
		xAxis.setTitle(new AxisTitle()
			.setText(boxPlotChart.getxLabel()));
		
		YAxis yAxis = new YAxis();
		yAxis.setTitle(new AxisTitle()
			.setText(boxPlotChart.getyLabel()));
		
		ChartTitle title = new ChartTitle()
			.setText(boxPlotChart.getTitle())
			.setAlign(ChartAlign.CENTER);
		
		Legend legend = new Legend()
			.setEnabled(true)
			.setAlign("center")
			.setLayout("horizontal")
			.setVerticalAlign("bottom");
		
		options.setChart(chart);
		options.setTitle(title);
		options.addxAxis(xAxis);
		options.addyAxis(yAxis);
		options.setCredits(new Credits());
		options.setLegend(legend);
		options.addSeries(HighchartsDataUtil.parseToBoxPlotSeriesList(
				boxPlotChart.getBoxPlotSeries()));
		options.addSeries(HighchartsDataUtil.parseToXYDataSeriesList(
				boxPlotChart.getxYDataSeries()));

		return options;
	}
	
	protected Options createXYDataChart(XYDataChart xYDataChart, ChartConstructorType chartConstructorType, Model model)
	{
		Options options = new Options();
		
		final BasicChart chart;
		if(ChartConstructorType.CHART.equals(chartConstructorType)) {
			chart = new Chart();
		} else {
			chart = new StockChart();
		}
		
		chart.setType(ChartType.getChartType(xYDataChart.getType()))
			.setWidth(xYDataChart.getWidth())
			.setHeight(xYDataChart.getHeight());
		
		XAxis xAxis = new XAxis();
		xAxis
			.setTitle(new AxisTitle()
				.setText(xYDataChart.getxAxisLabel())
				.setAlign(AxisAlign.MIDDLE))
			.setType(AxisType.valueOf(xYDataChart.getxAxisType().name()))
			.setOrdinal(false);
		
		YAxis yAxis = new YAxis();
		yAxis
			.setTitle(new AxisTitle()
				.setText(xYDataChart.getyAxisLabel())
				.setAlign(AxisAlign.MIDDLE))
			.setType(AxisType.valueOf(xYDataChart.getyAxisType().name()));
		
		ChartTitle title = new ChartTitle()
			.setText(xYDataChart.getTitle())
			.setAlign(ChartAlign.CENTER);
		
		Legend legend = new Legend()
			.setEnabled(true)
			.setAlign("center")
			.setLayout("horizontal")
			.setVerticalAlign("bottom");
		
		options.setChart(chart);
		options.setTitle(title);
		options.addxAxis(xAxis);
		options.addyAxis(yAxis);
		options.setCredits(new Credits());
		options.setLegend(legend);
		options.setSeries(HighchartsDataUtil.parseToXYDataSeriesList(
				xYDataChart.getData()));

		return options;
	}
}
