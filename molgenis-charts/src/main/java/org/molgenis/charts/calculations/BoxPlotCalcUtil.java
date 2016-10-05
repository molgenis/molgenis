package org.molgenis.charts.calculations;

import org.molgenis.charts.MolgenisChartException;

import java.util.List;

public class BoxPlotCalcUtil
{
	/**
	 * calculates the 5 values needed to create a box plot and returns them in an 5 item sized array.
	 * <p>
	 * Double[0] = minimum;
	 * Double[1] = firstQuantile;
	 * Double[2] = median;
	 * Double[3] = thirdQuantile;
	 * Double[4] = maximum;
	 *
	 * @param sortedDataAscendingOrder
	 * @return Double[]
	 */
	public static Double[] calcBoxPlotValues(List<Double> sortedDataAscendingOrder)
	{
		if (null == sortedDataAscendingOrder)
		{
			throw new MolgenisChartException("The sortedDataAscendingOrder list is null");
		}

		if (sortedDataAscendingOrder.isEmpty())
		{
			return new Double[] { 0d, 0d, 0d, 0d, 0d };
		}

		Double[] plotBoxValues = new Double[5];
		plotBoxValues[0] = minimum(sortedDataAscendingOrder);
		plotBoxValues[1] = firstQuantile(sortedDataAscendingOrder);
		plotBoxValues[2] = median(sortedDataAscendingOrder);
		plotBoxValues[3] = thirdQuantile(sortedDataAscendingOrder);
		plotBoxValues[4] = maximum(sortedDataAscendingOrder);

		return plotBoxValues;
	}

	/**
	 * IQR inner quartile range
	 *
	 * @param thirdQuantile
	 * @param firstQuantile
	 * @return
	 */
	public static double iqr(double thirdQuantile, double firstQuantile)
	{
		return thirdQuantile - firstQuantile;
	}

	/**
	 * Get the minimum value thru linear interpolations
	 *
	 * @param sortedDataAscendingOrder
	 * @return Double
	 */
	public static Double minimum(List<Double> sortedDataAscendingOrder)
	{
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, (double) 0);
	}

	/**
	 * Get the maximum value thru linear interpolations
	 *
	 * @param sortedDataAscendingOrder
	 * @return Double
	 */
	public static Double maximum(List<Double> sortedDataAscendingOrder)
	{
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, (double) 1);
	}

	/**
	 * Get the median value thru linear interpolations
	 *
	 * @param sortedDataAscendingOrder
	 * @return Double
	 */
	public static Double median(List<Double> sortedDataAscendingOrder)
	{
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, 0.50);
	}

	/**
	 * Get the firstQuantile value thru linear interpolations
	 *
	 * @param sortedDataAscendingOrder
	 * @return Double
	 */
	public static Double firstQuantile(List<Double> sortedDataAscendingOrder)
	{
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, 0.25);
	}

	/**
	 * Get the thirdQuantile value thru linear interpolations
	 *
	 * @param sortedDataAscendingOrder
	 * @return Double
	 */
	public static Double thirdQuantile(List<Double> sortedDataAscendingOrder)
	{
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, 0.75);
	}

	/**
	 * Interpolate linearly an quantile
	 * <p>
	 * Inspired on: http://msenux.redwoods.edu/math/R/boxplot.php
	 *
	 * @param sortedDataAscendingOrder sorted data in ascending order (NOT NULL)
	 * @param p                        percentage
	 * @return Double interpolated linearly quantile
	 */
	private static Double interpolateLinearlyQuantile(List<Double> sortedDataAscendingOrder, Double p)
	{
		int n = sortedDataAscendingOrder.size();
		double position = (1 + (p * (n - 1)));

		int leftIndex = (int) Math.floor(position) - 1;
		int rightIndex = (int) Math.ceil(position) - 1;
		Double quantile;

		if (leftIndex == rightIndex)
		{
			quantile = sortedDataAscendingOrder.get(leftIndex);
		}
		else
		{
			Double leftIndexValue = sortedDataAscendingOrder.get(leftIndex);
			Double rightIndexValue = sortedDataAscendingOrder.get(rightIndex);

			quantile = leftIndexValue + 0.5 * (rightIndexValue - leftIndexValue);
		}

		return quantile;
	}
}
