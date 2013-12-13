package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.charttypes.LineChart;
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
	
	LineChart getLineChart(String entityName, String attributeNameXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules);
	
	DataMatrix getDataMatrix(String entityName, List<String> attributeNamesXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules);
	
	Class<?> getAttributeJavaType(String entityName, String attributeName);
}
