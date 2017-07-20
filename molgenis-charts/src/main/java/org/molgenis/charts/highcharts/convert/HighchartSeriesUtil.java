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

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneOffset.UTC;

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
		List<Series> series = new ArrayList<>();
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
		List<Series> series = new ArrayList<>();
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
				AttributeType.DATE.equals(xYDataSerie.getAttributeXFieldTypeEnum()) || AttributeType.DATE_TIME.equals(
						xYDataSerie.getAttributeXFieldTypeEnum())))
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
		series.setData(new ArrayList<>(boxPlotSerie.getData()));
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
		List<Object> data = new ArrayList<>();
		for (XYData xYData : xydata)
		{
			List<Object> list = new ArrayList<>();
			list.add(convertValue(xValueFieldTypeEnum, xYData.getXvalue()));
			list.add(convertValue(yValueFieldTypeEnum, xYData.getYvalue()));
			data.add(list);
		}
		return data;
	}

	/**
	 * Convert values to match the Highcharts demand when using JSON.
	 *
	 * @param attributeType the type of the attribute
	 * @param value         the attribute value to convert
	 * @return Object the converted value
	 */
	public Object convertValue(AttributeType attributeType, Object value)
	{
		if (AttributeType.DATE_TIME.equals(attributeType))
		{
			return ((Instant) value).toEpochMilli();
		}
		else if (AttributeType.DATE.equals(attributeType))
		{
			return ((LocalDate) value).atStartOfDay(UTC).toInstant().toEpochMilli();
		}
		else
		{
			// Highcharts uses the string value of the x axis as the name of the point
			return value;
		}
	}

}
