package org.molgenis.charts.highcharts.convert;

import org.molgenis.charts.MolgenisSerieType;
import org.molgenis.charts.data.BoxPlotSerie;
import org.molgenis.charts.data.XYData;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.charts.highcharts.basic.Marker;
import org.molgenis.charts.highcharts.basic.Series;
import org.molgenis.charts.highcharts.basic.SeriesType;
import org.molgenis.data.meta.AttributeType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This data util is made for converting the Molgenis charts structure to the Highchart structure
 */
@Component
public class HighchartSeriesUtil
{
	/**
	 * Parse the xyDataSeries objects list to a Series objects list. The new series object can be used for xy data
	 * charts (like scatter plot)
	 *
	 * @param xYDataSeries
	 * @return Series
	 */
	public List<Series> parseToXYDataSeriesList(List<XYDataSerie> xYDataSeries)
	{
		List<Series> series = new ArrayList<Series>();
		for (XYDataSerie xYDataSerie : xYDataSeries)
		{
			series.add(parsexYDataSerieToSeries(xYDataSerie));
		}
		return series;
	}

	/**
	 * Parse the boxPlotSeries objects list to a Series objects list. The new series object can be used only for
	 * BoxPlotSeries
	 *
	 * @param boxPlotSeries
	 * @return series
	 */
	public List<Series> parseToBoxPlotSeriesList(List<BoxPlotSerie> boxPlotSeries)
	{
		List<Series> series = new ArrayList<Series>();
		for (BoxPlotSerie boxPlotSerie : boxPlotSeries)
		{
			series.add(parseBoxPlotSerieToSeries(boxPlotSerie));
		}
		return series;
	}

	/**
	 * Parse the xYDataSerie to a Series object computable with the Highcharts xy series standard.
	 *
	 * @param xYDataSerie
	 * @return Series
	 */
	public Series parsexYDataSerieToSeries(XYDataSerie xYDataSerie)
	{
		Series series = new Series();
		series.setName(xYDataSerie.getName());
		series.setType(SeriesType.getSeriesType(xYDataSerie.getType()));
		series.setData(parseXYDataToList(xYDataSerie.getData(), xYDataSerie.getAttributeXFieldTypeEnum(),
				xYDataSerie.getAttributeYFieldTypeEnum()));

		if (MolgenisSerieType.SCATTER.equals(xYDataSerie.getType()) && (
				AttributeType.DATE.equals(xYDataSerie.getAttributeXFieldTypeEnum()) || AttributeType.DATE_TIME
						.equals(xYDataSerie.getAttributeXFieldTypeEnum())))
		{
			series.setLineWidth(0);
			series.setMarker(new Marker(true, 4));
			series.setType(SeriesType.getSeriesType(MolgenisSerieType.LINE));
		}
		return series;
	}

	/**
	 * Parse the boxPlotSerie to a Series object computable with the Highcharts box plot series standard
	 *
	 * @param boxPlotSerie
	 * @return series
	 */
	public Series parseBoxPlotSerieToSeries(BoxPlotSerie boxPlotSerie)
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
	public List<Object> parseXYDataToList(List<XYData> xydata, AttributeType xValueFieldTypeEnum,
			AttributeType yValueFieldTypeEnum)
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

	/**
	 * Convert values to match the Highcharts demand when using json
	 *
	 * @param fieldTypeEnum
	 * @param value
	 * @return Object
	 */
	public Object convertValue(AttributeType fieldTypeEnum, Object value)
	{
		if (AttributeType.DATE_TIME.equals(fieldTypeEnum))
		{
			return (convertDateTimeToMilliseconds((Date) value));
		}
		else if (AttributeType.DATE.equals(fieldTypeEnum))
		{
			return (convertDateToMilliseconds((Date) value));
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
	 * <p>
	 * This can be a problem when accepting JavaSript to create a JavaScript Date object not knowing the time zone and
	 * ..
	 */
	public Long convertDateToMilliseconds(Date date)
	{
		if (date == null) return null;
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
	 * <p>
	 * This can be a problem when accepting JavaSript to create a JavaScript Date object not knowing the time zone and
	 * ..
	 */
	public Long convertDateTimeToMilliseconds(Date date)
	{
		if (date == null) return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		Calendar calendarConverted = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
		calendarConverted.clear();

		calendarConverted.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),
				calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

		return calendarConverted.getTimeInMillis();
	}
}
