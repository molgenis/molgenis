package org.molgenis.charts.highcharts.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.charts.data.XYData;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.charts.highcharts.ChartType;
import org.molgenis.charts.highcharts.Series;

/**
 * @author jonathanjetten
 */
public class HighchartsDataUtil
{
	private static final Logger logger = Logger.getLogger(HighchartsDataUtil.class);

	public static List<Series> parseToSeriesList(List<XYDataSerie> xYDataSeries, String type)
	{
		List<Series> series = new ArrayList<Series>();
		for (XYDataSerie xYDataSerie : xYDataSeries)
		{
			series.add(parseToSeries(xYDataSerie, type));
		}
		return series;
	}

	public static Series parseToSeries(XYDataSerie xYDataSerie, String type)
	{
		Series series = new Series();
		series.setName(xYDataSerie.getName());
		series.setType(type);
		series.setData(parseToXYDataList(xYDataSerie.getData()));
		return series;
	}

	public static List<Object> parseToXYDataList(List<XYData> xydata)
	{
		List<Object> data = new ArrayList<Object>();
		for (XYData xYData : xydata)
		{
			List<Number> tempPoint = new ArrayList<Number>();
			//TODO JJ
			tempPoint.add(new BigDecimal((String) xYData.getXvalue()));
			tempPoint.add(new BigDecimal((String) xYData.getYvalue()));
			data.add(tempPoint);
		}

		return data;
	}
}
