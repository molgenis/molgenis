package org.molgenis.charts.highcharts.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.charts.data.BoxPlotSerie;
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

	public static List<Series> parseToSeriesList(List<XYDataSerie> xYDataSeries, ChartType chartType)
	{
		List<Series> series = new ArrayList<Series>();
		for (XYDataSerie xYDataSerie : xYDataSeries)
		{
			series.add(parseToSeries(xYDataSerie, chartType));
		}
		return series;
	}
	
	public static List<Series> parseToBoxPlotSeriesList(List<BoxPlotSerie> boxPlotSeries)
	{
		List<Series> series = new ArrayList<Series>();
		for (BoxPlotSerie boxPlotSerie : boxPlotSeries)
		{
			series.add(parseToSeries(boxPlotSerie));
		}
		return series;
	}

	public static Series parseToSeries(XYDataSerie xYDataSerie, ChartType chartType)
	{
		Series series = new Series();
		series.setName(xYDataSerie.getName());
		series.setType(chartType);
		series.setData(parseToXYDataList(xYDataSerie.getData(), xYDataSerie.getAttributeXJavaType(), xYDataSerie.getAttributeYJavaType()));
		return series;
	}
	
	public static Series parseToSeries(BoxPlotSerie boxPlotSerie)
	{
		Series series = new Series();
		series.setName(boxPlotSerie.getName());
		series.setData(new ArrayList<Object>(boxPlotSerie.getData()));
		return series;
	}

	public static List<Object> parseToXYDataList(List<XYData> xydata, Class<?> xValueClass, Class<?> yValueClass)
	{		
		List<Object> data = new ArrayList<Object>();
		for (XYData xYData : xydata)
		{
			List<Object> tempPoint = new ArrayList<Object>();
			tempPoint.add(convertValue(xValueClass, xYData.getXvalue()));
			tempPoint.add(convertValue(yValueClass, xYData.getYvalue()));
			data.add(tempPoint);
		}

		return data;
	}

	public static Object convertValue(Class<?> clazz, Object value)
	{
		if (Date.class == clazz)
		{
			return (Long) ((Date) value).getTime();
		}
		else if (Timestamp.class == clazz)
		{
			return (Long) ((Timestamp) value).getTime();
		}
		else
		{
			return value;
		}
	}
}
