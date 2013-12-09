package org.molgenis.charts.highcharts;

import org.molgenis.charts.XYDataChart;
import org.molgenis.charts.highcharts.data.HighchartsDataUtil;
import org.molgenis.charts.highcharts.dataexplorer.requestpayload.LineChartRequestPayLoad;
import org.springframework.ui.Model;

/**
 * 
 * @author jonathanjetten
 *
 */
public class HighchartService
{
	public Options createLine(LineChartRequestPayLoad request, XYDataChart xYDataChart, Model model)
	{
		Options options = new Options();
		
		Chart chart = new Chart();
		chart.setType("column");
		chart.setWidth(request.getWidth());
		chart.setHeight(request.getHeight());
		
		Title title = new Title();
		title.setText(request.getTitle());
		
		XAxis xAxis = new XAxis();
		xAxis.setTitle(new Title()).setText(request.getAttributeNameXaxis());
		
		YAxis yAxis = new YAxis();
		yAxis.setTitle(new Title()).setText(request.getAttributeNameYaxis());
		
		options.setSeries(HighchartsDataUtil.parseToSeriesList(xYDataChart.getXYDataSeries(), "line"));
		options.setChart(chart);
		options.setTitle(title);
		options.addxAxis(xAxis);
		options.addyAxis(yAxis);

		return options;
	}
}
