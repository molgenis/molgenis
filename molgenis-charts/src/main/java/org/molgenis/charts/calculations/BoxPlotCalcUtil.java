package org.molgenis.charts.calculations;

import java.util.List;

public class BoxPlotCalcUtil
{
	public final static Double[] calcPlotBoxValues(List<Double> sortedDataAscendingOrder){
		if(null != sortedDataAscendingOrder 
				&& sortedDataAscendingOrder.size() < 5) {
			//TODO JJ throws exception
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
	public final static double iqr(double thirdQuantile, double firstQuantile)
	{
		return thirdQuantile - firstQuantile;
	}
	
	public final static Double minimum(List<Double> sortedDataAscendingOrder){
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, (double) 0);
	}
	
	public final static Double maximum(List<Double> sortedDataAscendingOrder){
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, (double) 1);
	}
	
	public final static Double median(List<Double> sortedDataAscendingOrder){
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, 0.50);
	}
	
	public final static Double firstQuantile(List<Double> sortedDataAscendingOrder){
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, 0.25);
	}
	
	public final static Double thirdQuantile(List<Double> sortedDataAscendingOrder){
		return interpolateLinearlyQuantile(sortedDataAscendingOrder, 0.75);
	}
	
	/**
	 * Interpolate linearly an quantile
	 * 
	 * @param sortedDataAscendingOrder sorted data in ascending order (NOT NULL) 
	 * @param p percentage
	 * @return Double interpolated linearly quantile
	 */
	private final static Double interpolateLinearlyQuantile(List<Double> sortedDataAscendingOrder, Double p) 
	{	
		int n = sortedDataAscendingOrder.size();
		double position = (1 + (p * (n - 1)));
	
		int leftIndex = (int) Math.floor(position) - 1;
		int rightIndex = (int) Math.ceil(position) - 1;
		Double quantile;

		if(leftIndex == rightIndex){
			quantile = sortedDataAscendingOrder.get(leftIndex);
		}else{
			Double leftIndexValue = sortedDataAscendingOrder.get(leftIndex);
			Double rightIndexValue = sortedDataAscendingOrder.get(rightIndex);
			
			quantile = leftIndexValue + 0.5 * (rightIndexValue - leftIndexValue);
		}

		return quantile;
	}
}
