package org.molgenis.charts.highcharts.basic;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.MolgenisAxisType;
import org.molgenis.charts.XYDataChart;
import org.molgenis.charts.data.XYData;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.charts.highcharts.chart.Chart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.Model;
import org.testng.annotations.Test;

@ContextConfiguration
public class HighchartServiceTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	public static class Config
	{
		@Bean
		public HighchartService highchartsService()
		{
			return new HighchartService();
		}
	}
	
	@Autowired
	private HighchartService highchartService;
	
	@Test //XYDataChart xYDataChart, Model model
	public void createXYDataChart(){
		List<XYDataSerie> data = new ArrayList<XYDataSerie>();
		XYDataSerie serieOne = new XYDataSerie();
		XYData xYDataOne = new XYData(Integer.valueOf("1"), Double.valueOf("1.1"));
		XYData xYDataTwo = new XYData(Integer.valueOf("2"), Double.valueOf("2.2"));
		XYData xYDataThree = new XYData(Integer.valueOf("3"), Double.valueOf("3.3"));
		serieOne.addData(xYDataOne);
		serieOne.addData(xYDataTwo);
		serieOne.addData(xYDataThree);
		data.add(serieOne);
		MolgenisAxisType xAxisType = MolgenisAxisType.LINEAR;
		MolgenisAxisType yAxisType = MolgenisAxisType.LINEAR;
		XYDataChart xYDataChart = new XYDataChart(data, xAxisType, yAxisType);
		xYDataChart.setType(MolgenisChartType.SCATTER_CHART);
		xYDataChart.setxAxisLabel("xlabel");
		xYDataChart.setyAxisLabel("ylabel");
		xYDataChart.setTitle("title");
		Model model = null;
		
		Options options = (Options) highchartService.renderChartInternal(xYDataChart, model);
		
		//Tests
		assertNotNull(options);
		assertTrue(options.getChart() instanceof Chart);
		
		// Axis
		assertNotNull(options.getxAxis());
		assertFalse(options.getxAxis().isEmpty());
		assertNotNull(options.getyAxis());
		assertFalse(options.getxAxis().isEmpty());
		assertEquals(AxisType.LINEAR.toString(), options.getxAxis().get(0).getType());
		assertEquals(AxisType.LINEAR.toString(), options.getyAxis().get(0).getType());
		assertEquals(Boolean.valueOf(false), options.getxAxis().get(0).getOrdinal());
		assertEquals("xlabel", options.getxAxis().get(0).getTitle().getText());
		assertEquals("ylabel", options.getyAxis().get(0).getTitle().getText());
		assertEquals(AxisAlign.MIDDLE.toString(), options.getxAxis().get(0).getTitle().getAlign());
		assertEquals(AxisAlign.MIDDLE.toString(), options.getyAxis().get(0).getTitle().getAlign());
		
		// Title
		assertNotNull(options.getTitle());
		assertEquals("title", options.getTitle().getText());
		assertEquals(ChartAlign.CENTER, options.getTitle().getAlign());
		
		// Legend
		assertNotNull(options.getLegend());
		assertEquals(Boolean.valueOf(true), options.getLegend().getEnabled());
		assertEquals("center", options.getLegend().getAlign());
		assertEquals("horizontal", options.getLegend().getLayout());
		assertEquals("bottom", options.getLegend().getVerticalAlign());		
		
		// Credit
		assertNotNull(options.getCredits());
		
		//Series
		assertNotNull(options.getSeries());
		assertFalse(options.getSeries().isEmpty());
		assertEquals(SeriesType.SCATTER.toString(), options.getSeries().get(0).getType());
		assertEquals(Arrays.<Object>asList(Integer.valueOf("1"), Double.valueOf("1.1")), options.getSeries().get(0).getData().get(0));;
		assertEquals(Arrays.<Object>asList(Integer.valueOf("2"), Double.valueOf("2.2")), options.getSeries().get(0).getData().get(1));;
		assertEquals(Arrays.<Object>asList(Integer.valueOf("3"), Double.valueOf("3.3")), options.getSeries().get(0).getData().get(2));;
	}
}
