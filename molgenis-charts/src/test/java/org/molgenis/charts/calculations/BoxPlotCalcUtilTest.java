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
		List<Double> sortedDataAscendingOrder = new ArrayList<Double>();
		sortedDataAscendingOrder.addAll(
				Arrays.asList(new Double(0), new Double(1), new Double(1), new Double(3), new Double(3), new Double(4),
						new Double(5), new Double(6), new Double(8), new Double(15), new Double(20)));
		Double[] plotBoxValues = BoxPlotCalcUtil.calcBoxPlotValues(sortedDataAscendingOrder);
		assertEquals(plotBoxValues[0], new Double(0));
		assertEquals(plotBoxValues[1], new Double(2));
		assertEquals(plotBoxValues[2], new Double(4));
		assertEquals(plotBoxValues[3], new Double(7));
		assertEquals(plotBoxValues[4], new Double(20));
	}

	@Test
	public void testCalcPlotBoxValuesNonEven()
	{
		List<Double> sortedDataAscendingOrder = new ArrayList<Double>();
		sortedDataAscendingOrder.addAll(
				Arrays.asList(new Double(0.5), new Double(1.5), new Double(1.5), new Double(3.5), new Double(3.5),
						new Double(4.5), new Double(5.5), new Double(5.5), new Double(6.5), new Double(8.5),
						new Double(15.5), new Double(20.5)));
		Double[] plotBoxValues = BoxPlotCalcUtil.calcBoxPlotValues(sortedDataAscendingOrder);
		assertEquals(plotBoxValues[0], new Double(0.5));
		assertEquals(plotBoxValues[1], new Double(2.5));
		assertEquals(plotBoxValues[2], new Double(5));
		assertEquals(plotBoxValues[3], new Double(7.5));
		assertEquals(plotBoxValues[4], new Double(20.5));
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
		List<Double> sortedDataAscendingOrder = new ArrayList<Double>();
		assertEquals(BoxPlotCalcUtil.calcBoxPlotValues(sortedDataAscendingOrder), new Double[] { 0d, 0d, 0d, 0d, 0d });
	}
}

