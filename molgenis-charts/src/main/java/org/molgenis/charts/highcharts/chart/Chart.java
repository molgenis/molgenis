package org.molgenis.charts.highcharts.chart;

import org.molgenis.charts.highcharts.basic.BasicChart;

public class Chart extends BasicChart
{
	// Default values
	private static final Integer MARGIN_BOTTOM = 100;
	private static final Integer MARGIN_LEFT = 100;
	private static final Integer MARGIN_RIGHT = 50;
	private static final Integer MARGIN_TOP = 50;

	public Chart()
	{
		this.setMarginBottom(MARGIN_BOTTOM)
			.setMarginLeft(MARGIN_LEFT)
			.setMarginRight(MARGIN_RIGHT)
			.setMarginTop(MARGIN_TOP);
	}
}
