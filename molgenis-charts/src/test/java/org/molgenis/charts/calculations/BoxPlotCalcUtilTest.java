package org.molgenis.charts.calculations;

import org.molgenis.charts.MolgenisChartException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class BoxPlotCalcUtilTest
{
	@Test
	public void testCalcPlotBoxValuesEven()
	{
		List<Double> sortedDataAscendingOrder = new ArrayList<>();
		sortedDataAscendingOrder.addAll(
				Arrays.asList(0d, 1d, 1d, 3d, 3d, 4d,
						5d, 6d, 8d, 15d, 20d));
		Double[] plotBoxValues = BoxPlotCalcUtil.calcBoxPlotValues(sortedDataAscendingOrder);
		assertEquals(plotBoxValues[0], 0d);
		assertEquals(plotBoxValues[1], 2d);
		assertEquals(plotBoxValues[2], 4d);
		assertEquals(plotBoxValues[3], 7d);
		assertEquals(plotBoxValues[4], 20d);
	}

	@Test
	public void testCalcPlotBoxValuesNonEven()
	{
		List<Double> sortedDataAscendingOrder = new ArrayList<>();
		sortedDataAscendingOrder.addAll(
				Arrays.asList(0.5, 1.5, 1.5, 3.5, 3.5, 4.5, 5.5, 5.5, 6.5, 8.5, 15.5, 20.5));
		Double[] plotBoxValues = BoxPlotCalcUtil.calcBoxPlotValues(sortedDataAscendingOrder);
		assertEquals(plotBoxValues[0], 0.5);
		assertEquals(plotBoxValues[1], 2.5);
		assertEquals(plotBoxValues[2], 5d);
		assertEquals(plotBoxValues[3], 7.5);
		assertEquals(plotBoxValues[4], 20.5);
	}

	@Test(expectedExceptions = MolgenisChartException.class)
	public void testCalcPlotBoxValuesListIsNull()
	{
		List<Double> sortedDataAscendingOrder = null;
		BoxPlotCalcUtil.calcBoxPlotValues(sortedDataAscendingOrder);
	}

	@Test()
	public void testCalcPlotBoxValuesListIsEmpty()
	{
		List<Double> sortedDataAscendingOrder = new ArrayList<>();
		assertEquals(BoxPlotCalcUtil.calcBoxPlotValues(sortedDataAscendingOrder), new Double[] { 0d, 0d, 0d, 0d, 0d });
	}
}

