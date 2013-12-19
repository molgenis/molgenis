package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.data.DataMatrix;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.data.QueryRule;

/**
 * Retrieves chart data for rendering
 */
public interface ChartDataService
{
	XYDataSerie getXYDataSerie(String entityName, String attributeNameXaxis, String attributeNameYaxis,
			Class<?> attributeXJavaType, Class<?> attributeYJavaType, List<QueryRule> queryRules);
	
	List<XYDataSerie> getXYDataSeries(String entityName, String attributeNameXaxis, String attributeNameYaxis,
			Class<?> attributeXJavaType, Class<?> attributeYJavaType, String split, List<QueryRule> queryRules);
	
	XYDataChart getXYDataChart(String entityName, String attributeNameXaxis, String attributeNameYaxis,
			String split, List<QueryRule> queryRules);
	
	BoxPlotChart getBoxPlotChart(String entityName, String attributeName, List<QueryRule> queryRules);
	
	DataMatrix getDataMatrix(String entityName, List<String> attributeNamesXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules);
}
