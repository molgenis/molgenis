package org.molgenis.charts.highcharts.convert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.charts.MolgenisSerieType;
import org.molgenis.charts.data.BoxPlotSerie;
import org.molgenis.charts.data.XYData;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.charts.highcharts.basic.Series;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration
public class HighchartSeriesUtilTest
{	
	@Autowired
	private HighchartSeriesUtil highchartSeriesUtil;
	
	@BeforeMethod
	public void setUp()
	{
		highchartSeriesUtil = new HighchartSeriesUtil();
	}
	
	@Test
	public void convertDateTimeToMilliseconds()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2014, 1, 1, 1, 1, 1);
		final TimeZone timeZone = calendar.getTimeZone();
		long offset = timeZone.getOffset(calendar.getTime().getTime());
		long correctResult = offset + calendar.getTime().getTime();
		assertEquals(highchartSeriesUtil.convertDateTimeToMilliseconds(calendar.getTime()), correctResult);
	}
	
	@Test
	public void convertDateToMilliseconds()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2014, 1, 1, 1, 1, 1);
		final TimeZone timeZone = calendar.getTimeZone();
		long offset = timeZone.getOffset(calendar.getTime().getTime());
		long notCorrectResult = offset + calendar.getTime().getTime();
		Long correctResult = offset + calendar.getTime().getTime() - 3661000l;
		assertNotEquals(highchartSeriesUtil.convertDateToMilliseconds(calendar.getTime()), notCorrectResult);
		assertEquals(highchartSeriesUtil.convertDateToMilliseconds(calendar.getTime()), correctResult);
	}
	
	@Test
	public void convertValueDateTime() {
		FieldTypeEnum fieldTypeEnum = FieldTypeEnum.DATE_TIME;
		Date value = mock(Date.class);
		assertTrue(highchartSeriesUtil.convertValue(fieldTypeEnum, value) instanceof Long);
	}
	
	@Test
	public void convertValueDate() {
		FieldTypeEnum fieldTypeEnum = FieldTypeEnum.DATE;
		Date value = mock(Date.class);
		assertTrue(highchartSeriesUtil.convertValue(fieldTypeEnum, value) instanceof Long);
	}
	
	@Test
	public void convertValueString() {
		FieldTypeEnum fieldTypeEnum = FieldTypeEnum.STRING;
		String value = "test";
		assertTrue(highchartSeriesUtil.convertValue(fieldTypeEnum, value) instanceof String);
		assertEquals(highchartSeriesUtil.convertValue(fieldTypeEnum, value), value);
	}
	
	@Test
	public void convertValueInt() {
		FieldTypeEnum fieldTypeEnum = FieldTypeEnum.INT;
		Integer value = Integer.valueOf("1");
		assertTrue(highchartSeriesUtil.convertValue(fieldTypeEnum, value) instanceof Integer);
		assertEquals(highchartSeriesUtil.convertValue(fieldTypeEnum, value), value);
	}
	
	@Test
	public void parseXYDataToList() {
		FieldTypeEnum xValueFieldTypeEnum = FieldTypeEnum.DECIMAL;
		FieldTypeEnum yValueFieldTypeEnum = FieldTypeEnum.DECIMAL;
		Double xvalue = Double.valueOf("1.1");
		Double yvalue = Double.valueOf("2.2");
		List<XYData> xydata = new ArrayList<XYData>();
		xydata.add(new XYData(xvalue, yvalue));
		List<Object> list = highchartSeriesUtil.parseXYDataToList(xydata, xValueFieldTypeEnum, yValueFieldTypeEnum);
		assertTrue(list.size() == 1);
		assertTrue(((List<Object>) list.get(0)).size() == 2);
	}
	
	@Test
	public void parseBoxPlotSerieToSeries() {
		final String name = "test";
		final List<Double[]> listOfDoubleArrays = Arrays.<Double[]>asList(new Double[]{0d,0d,0d,0d,0d});
		BoxPlotSerie boxPlotSerie = mock(BoxPlotSerie.class);
		when(boxPlotSerie.getName()).thenReturn(name);
		when(boxPlotSerie.getData()).thenReturn(listOfDoubleArrays);
		Series series = highchartSeriesUtil.parseBoxPlotSerieToSeries(boxPlotSerie);
		assertEquals(series.getName(), name);
		assertEquals(series.getData(), listOfDoubleArrays);
	}

	@Test
	public void parsexYDataSerieToSeries() {
		final String name = "test";
		final MolgenisSerieType molgenisSerieType = MolgenisSerieType.SCATTER;
		XYDataSerie xYDataSerie = mock(XYDataSerie.class);
		when(xYDataSerie.getName()).thenReturn(name);
		when(xYDataSerie.getType()).thenReturn(molgenisSerieType);
		when(xYDataSerie.getAttributeXFieldTypeEnum()).thenReturn(FieldTypeEnum.DECIMAL);
		when(xYDataSerie.getAttributeYFieldTypeEnum()).thenReturn(FieldTypeEnum.DECIMAL);
		Series series = highchartSeriesUtil.parsexYDataSerieToSeries(xYDataSerie);
		assertEquals(series.getName(), name);
		assertEquals(series.getType(), "scatter");
		assertNull(series.getMarker());
		assertNull(series.getLineWidth());
	}
	
}
