package org.molgenis.charts.highcharts;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.molgenis.charts.AbstractChart;
import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.AbstractChartVisualizationService;
import org.molgenis.charts.BoxPlotChart;
import org.molgenis.charts.XYDataChart;
import org.molgenis.charts.highcharts.data.HighchartsDataUtil;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/**
 * 
 * @author jonathanjetten
 * 
 */
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
		logger.info("renderChartInternal() --- chart.getType(): " + chart.getType());
		
		if (MolgenisChartType.LINE_CHART.equals(chart.getType()))
		{
			return this.createLineChart((XYDataChart) chart, model);
		}
		else if (MolgenisChartType.SCATTER_CHART.equals(chart.getType()))
		{
			return this.createScatterChart((XYDataChart) chart, model);
		}
		else if (MolgenisChartType.BOXPLOT_CHART.equals(chart.getType()))
		{
			return this.createBoxPlotChart((BoxPlotChart) chart, model);
		}
		return null;
	}
	
	private Options createScatterChart(XYDataChart scatterChart, Model model)
	{
		return createXYDataChart(scatterChart, model);
	}
	
	private Options createLineChart(XYDataChart lineChart, Model model)
	{
		return createXYDataChart(lineChart, model);
	}
	
	private Options createBoxPlotChart(BoxPlotChart boxPlotChart, Model model)
	{
		Options options = new Options();
		
		Chart chart = new Chart();
		chart.setType(ChartType.BOXPLOT)
			.setWidth(boxPlotChart.getWidth())
			.setHeight(boxPlotChart.getHeight());
		
		XAxis xAxis = new XAxis();
		
		YAxis yAxis = new YAxis();
		
		ChartTitle title = new ChartTitle()
			.setText("TEST")
			.setAlign(ChartAlign.CENTER);
		
		options.setSeries(HighchartsDataUtil.parseToBoxPlotSeriesList(
				boxPlotChart.getSeries()));
		options.setChart(chart);
		options.setTitle(title);
		options.addxAxis(xAxis);
		options.addyAxis(yAxis);
		options.setCredits(new Credits());

		return options;
	}
	
	private Options createXYDataChart(XYDataChart xYDataChart, Model model)
	{
		Options options = new Options();
		
		Chart chart = new Chart();
		chart.setType(ChartType.getChartType(xYDataChart.getType()))
			.setWidth(xYDataChart.getWidth())
			.setHeight(xYDataChart.getHeight());
		
		XAxis xAxis = new XAxis();
		xAxis
			.setTitle(new AxisTitle()
				.setText(xYDataChart.getxAxisLabel())
				.setAlign(AxisAlign.MIDDLE))
			.setType(AxisType.valueOf(xYDataChart.getxAxisType().name()));
		
		YAxis yAxis = new YAxis();
		yAxis
			.setTitle(new AxisTitle()
				.setText(xYDataChart.getyAxisLabel())
				.setAlign(AxisAlign.MIDDLE))
			.setType(AxisType.valueOf(xYDataChart.getyAxisType().name()));
		
		ChartTitle title = new ChartTitle()
			.setText(xYDataChart.getTitle())
			.setAlign(ChartAlign.CENTER);
		
		options.setSeries(HighchartsDataUtil.parseToSeriesList(
				xYDataChart.getData(), 
				ChartType.getChartType(xYDataChart.getType())));
		options.setChart(chart);
		options.setTitle(title);
		options.addxAxis(xAxis);
		options.addyAxis(yAxis);
		options.setCredits(new Credits());

		return options;
	}
}
