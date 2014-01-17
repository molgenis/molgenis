package org.molgenis.charts;

import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.charts.data.DataMatrix;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;

/**
 * Retrieves chart data for rendering
 */
public interface ChartDataService
{	
	XYDataChart getXYDataChart(String entityName, String attributeNameXaxis, String attributeNameYaxis,
			String split, List<QueryRule> queryRules);
	
	DataMatrix getDataMatrix(String entityName, List<String> attributeNamesXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules);

	List<XYDataSerie> getXYDataSeries(Repository<? extends Entity> repo, String entityName, String attributeNameXaxis,
			String attributeNameYaxis, FieldTypeEnum attributeXFieldTypeEnum, FieldTypeEnum attributeYFieldTypeEnum, String split,
			List<QueryRule> queryRules);

	XYDataSerie getXYDataSerie(Repository<? extends Entity> repo, String entityName, String attributeNameXaxis,
			String attributeNameYaxis, FieldTypeEnum attributeXFieldTypeEnum, FieldTypeEnum attributeYFieldTypeEnum,
			List<QueryRule> queryRules);

	BoxPlotChart getBoxPlotChart(String entityName, String attributeName, List<QueryRule> queryRules, String split,
			double scaleToCalcOutliers);
}
