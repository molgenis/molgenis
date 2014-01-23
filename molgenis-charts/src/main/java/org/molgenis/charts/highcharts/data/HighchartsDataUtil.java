package org.molgenis.charts.highcharts.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.charts.MolgenisSerieType;
import org.molgenis.charts.data.BoxPlotSerie;
import org.molgenis.charts.data.XYData;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.charts.highcharts.basic.Marker;
import org.molgenis.charts.highcharts.basic.Series;
import org.molgenis.charts.highcharts.basic.SeriesType;

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

	/**
	 * Parse the xYDataSerie to a Series object computable with the Highcharts standard.
	 * 
	 * @param xYDataSerie
	 * @return Series
	 */
	public static Series parseToSeries(XYDataSerie xYDataSerie)
	{
		Series series = new Series();
		series.setName(xYDataSerie.getName());
		series.setType(SeriesType.getSeriesType(xYDataSerie.getType()));
		series.setData(parseXYDataToList(xYDataSerie.getData(), xYDataSerie.getAttributeXFieldTypeEnum(),
				xYDataSerie.getAttributeYFieldTypeEnum()));

		if (MolgenisSerieType.SCATTER.equals(xYDataSerie.getType())
				&& (FieldTypeEnum.DATE.equals(xYDataSerie.getAttributeXFieldTypeEnum()) || FieldTypeEnum.DATE_TIME.equals(xYDataSerie.getAttributeXFieldTypeEnum())))
		{	
			series.setLineWidth(0);
			series.setMarker(new Marker(true, 4));
			series.setType(SeriesType.getSeriesType(MolgenisSerieType.LINE));
		}
		return series;
	}

	public static Series parseToSeries(BoxPlotSerie boxPlotSerie)
	{
		Series series = new Series();
		series.setName(boxPlotSerie.getName());
		series.setData(new ArrayList<Object>(boxPlotSerie.getData()));
		return series;
	}
	
	/**
	 * Parse the x and y data-objects to object computable with the Highcharts scatter plot standard.
	 * 
	 * @param xydata
	 * @param xValueFieldTypeEnum
	 * @param yValueFieldTypeEnum
	 * @return List<Object>
	 */
	public static List<Object> parseXYDataToList(List<XYData> xydata, FieldTypeEnum xValueFieldTypeEnum, FieldTypeEnum yValueFieldTypeEnum)
	{
		List<Object> data = new ArrayList<Object>();
		for (XYData xYData : xydata)
		{
			List<Object> list = new ArrayList<Object>();
			list.add(convertValue(xValueFieldTypeEnum, xYData.getXvalue()));
			list.add(convertValue(yValueFieldTypeEnum, xYData.getYvalue()));
			data.add(list);
		}
		return data;
	}

	public static Object convertValue(FieldTypeEnum fieldTypeEnum, Object value)
	{
		if (FieldTypeEnum.DATE_TIME.equals(fieldTypeEnum))
		{
			return (Long) (convertDateTimeToMilliseconds((Date) value));
		}
		else if (FieldTypeEnum.DATE.equals(fieldTypeEnum))
		{
			return (Long) (convertDateToMilliseconds((Date) value));
		}
		else
		{
			// Highcharts uses the string value of the x axis as the name of the point
			return value;
		}
	}

	/**
	 * Convert date to long keeping the timezone valued date. When asking the time of a Date object java return the
	 * milliseconds from the begin of counting. "Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
	 * represented by this Date object."
	 * 
	 * This can be a problem when accepting JavaSript to create a JavaScript Date object not knowing the time zone and
	 * ..
	 */
	public static long convertDateToMilliseconds(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		Calendar calendarConverted = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
		calendarConverted.clear();
		calendarConverted.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
		return calendarConverted.getTimeInMillis();
	}

	/**
	 * Convert date to long keeping the timezone valued date. When asking the time of a Date object java return the
	 * milliseconds from the begin of counting. "Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
	 * represented by this Timestamp object."
	 * 
	 * This can be a problem when accepting JavaSript to create a JavaScript Date object not knowing the time zone and
	 * ..
	 */
	public static long convertDateTimeToMilliseconds(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		Calendar calendarConverted = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
		calendarConverted.clear();

		calendarConverted.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),
				calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

		return calendarConverted.getTimeInMillis();
	}
}
