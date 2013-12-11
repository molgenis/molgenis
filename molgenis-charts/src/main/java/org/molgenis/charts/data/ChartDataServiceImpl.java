package org.molgenis.charts.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.charts.ChartDataService;
import org.molgenis.charts.MolgenisChartException;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ChartDataServiceImpl implements ChartDataService
{
	private final DataService dataService;
	private static final Logger logger = Logger.getLogger(ChartDataServiceImpl.class);

	@Autowired
	public ChartDataServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;

	}

	@Override
	public XYDataSerie getXYDataSerie(String entityName, String attributeNameXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules)
	{
		Repository<? extends Entity> repo = dataService.getRepositoryByEntityName(entityName);
		XYDataSerie serie = new XYDataSerie();
		serie.setName(attributeNameYaxis);
		
		//TODO JJ type detecting
		//FieldType fieldType = repo.getAttribute(attributeNameXaxis).getDataType();
		
		if(repo instanceof Queryable){
			if (queryRules == null)
			{
				queryRules = new ArrayList<QueryRule>();
			}
			Query q = new QueryImpl(queryRules);
			Sort sort = new Sort(Sort.DEFAULT_DIRECTION, attributeNameXaxis, attributeNameYaxis);
			q.sort(sort);
			
			Iterable<? extends Entity> iterable = ((Queryable<? extends Entity>) repo).findAll(q);
			
			for (Entity entity : iterable)
			{
				Object x = entity.get(attributeNameXaxis);
				Object y = entity.get(attributeNameYaxis);
				
				serie.addData(new XYData(x, y));
			}
					
			logger.info("repo instanceof Queryable");
			
		} else {
			//TODO JJ
			throw new MolgenisChartException("entity: " + entityName + " is not queryable and is not supported");
			
			
			// TODO JJ fix me
			// Excel documents are not supported because they are not Queryable
//			if (queryRules != null && !queryRules.isEmpty()) {
//				throw new MolgenisChartException("There a query rules defined but the " + entityName
//						+ " repository is not queryable");
//			}
//			
//			for (Entity entity : repo)
//			{
//				Object x = entity.get(attributeNameXaxis);
//				Object y = entity.get(attributeNameYaxis);
//				
//				serie.addData(new XYData(x, y));
//			}
		}
		
		logger.info("Type: " + repo.getAttribute(attributeNameXaxis).getDataType());
		logger.info("serie.getData().size(): " + serie.getData().size());

		return serie;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DataMatrix getDataMatrix(String entityName, List<String> attributeNamesXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules)
	{
		// dataService
		// .registerEntitySource("excel:///Users/erwin/projects/molgenis/molgenis-charts/src/test/resources/heatmap.xlsx");

		Iterable<? extends Entity> iterable = dataService.getRepositoryByEntityName(entityName);

		if (queryRules != null && !queryRules.isEmpty())
		{
			if (!(iterable instanceof Queryable))
			{
				throw new MolgenisChartException("There a query rules defined but the " + entityName
						+ " repository is not queryable");
			}

			QueryImpl q = new QueryImpl();
			for (QueryRule queryRule : queryRules)
			{
				q.addRule(queryRule);
			}

			iterable = ((Queryable<? extends Entity>) iterable).findAll(q);
		}

		List<Target> rowTargets = new ArrayList<Target>();
		List<Target> columnTargets = new ArrayList<Target>();
		List<List<Number>> values = new ArrayList<List<Number>>();

		for (String columnTargetName : attributeNamesXaxis)
		{
			columnTargets.add(new Target(columnTargetName));
		}

		for (Entity entity : iterable)
		{
			String rowTargetName = entity.getString(attributeNameYaxis) != null ? entity.getString(attributeNameYaxis) : "";
			rowTargets.add(new Target(rowTargetName));

			List<Number> rowValues = new ArrayList<Number>();
			for (String attr : attributeNamesXaxis)
			{
				rowValues.add(entity.getDouble(attr));
			}
			values.add(rowValues);
		}

		return new DataMatrix(columnTargets, rowTargets, values);
	}

}
