package org.molgenis.charts.highcharts.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.charts.data.BoxPlotSerie;
import org.molgenis.charts.data.XYData;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.charts.highcharts.Series;
import org.molgenis.charts.highcharts.SeriesType;

public class HighchartsDataUtil
{
	private static final Logger logger = Logger.getLogger(HighchartsDataUtil.class);

	public static List<Series> parseToXYDataSeriesList(List<XYDataSerie> xYDataSeries)
	{
		List<Series> series = new ArrayList<Series>();
		for (XYDataSerie xYDataSerie : xYDataSeries)
		{
			series.add(parseToSeries(xYDataSerie));
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

	public static Series parseToSeries(XYDataSerie xYDataSerie)
	{
		Series series = new Series();
		series.setName(xYDataSerie.getName());
		series.setType(SeriesType.getSeriesType(xYDataSerie.getType()));
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
		else if (String.class == clazz)
		{
			//Highcharts uses the string value of the x axis as the name of the point
			return value;
		}
		else
		{
			return value;
		}
	}
}
