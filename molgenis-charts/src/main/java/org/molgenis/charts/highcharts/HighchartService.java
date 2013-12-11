package org.molgenis.charts.highcharts;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.molgenis.charts.AbstractChart;
import org.molgenis.charts.AbstractChart.ChartType;
import org.molgenis.charts.AbstractChartVisualizationService;
import org.molgenis.charts.charttypes.LineChart;
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
		super(Arrays.asList(ChartType.LINE_CHART, ChartType.SCATTER_CHART, ChartType.BOXPLOT_CHART));
	}

	@Override
	protected Object renderChartInternal(AbstractChart chart, Model model)
	{
		if(ChartType.LINE_CHART.equals(chart.getType())){
			return this.createLine((LineChart) chart, model);
		}
		return null;
	}
	
	private Options createLine(LineChart lineChart, Model model)
	{
		Options options = new Options();
		
		Chart chart = new Chart();
		chart.setType("scatter");
		chart.setWidth(lineChart.getWidth());
		chart.setHeight(lineChart.getHeight());
		
		Title title = new Title();
		title.setText(lineChart.getTitle());
		
		XAxis xAxis = new XAxis();
		xAxis.setTitle(new Title()).setText(lineChart.getxAxisLabel());
		
		YAxis yAxis = new YAxis();
		yAxis.setTitle(new Title()).setText(lineChart.getxYxisLabel());
		
		options.setSeries(HighchartsDataUtil.parseToSeriesList(lineChart.getData(), "scatter"));
		options.setChart(chart);
		options.setTitle(title);
		options.addxAxis(xAxis);
		options.addyAxis(yAxis);
		
		logger.info("lineChart.getxAxisLabel(): " + lineChart.getxAxisLabel());

		return options;
	}
}
