package org.molgenis.charts;

import org.molgenis.charts.data.DataMatrix;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeType;

import java.util.List;

/**
 * Retrieves chart data for rendering
 */
public interface ChartDataService
{
	XYDataChart getXYDataChart(String entityName, String attributeNameXaxis, String attributeNameYaxis, String split,
			List<QueryRule> queryRules);

	DataMatrix getDataMatrix(String entityName, List<String> attributeNamesXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules);

	List<XYDataSerie> getXYDataSeries(Repository<Entity> repo, String entityName, String attributeNameXaxis,
			String attributeNameYaxis, AttributeType attributeXFieldTypeEnum, AttributeType attributeYFieldTypeEnum,
			String split, List<QueryRule> queryRules);

	XYDataSerie getXYDataSerie(Repository<Entity> repo, String entityName, String attributeNameXaxis,
			String attributeNameYaxis, AttributeType attributeXFieldTypeEnum, AttributeType attributeYFieldTypeEnum,
			List<QueryRule> queryRules);

	BoxPlotChart getBoxPlotChart(String entityName, String attributeName, List<QueryRule> queryRules, String split,
			double scaleToCalcOutliers);
}
