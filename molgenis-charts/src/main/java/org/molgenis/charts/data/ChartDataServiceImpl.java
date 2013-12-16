package org.molgenis.charts.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.charts.ChartDataService;
import org.molgenis.charts.MolgenisAxisType;
import org.molgenis.charts.MolgenisChartException;
import org.molgenis.charts.XYDataChart;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.model.MolgenisModelException;
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
	public XYDataChart getXYDataChart(String entityName, String attributeNameXaxis, String attributeNameYaxis, List<QueryRule> queryRules) 
	{
		Repository<? extends Entity> repo = dataService.getRepositoryByEntityName(entityName);
		
		try
		{
			Class<?> attributeXJavaType = repo.getAttribute(attributeNameXaxis).getDataType().getJavaType();
			Class<?> attributeYJavaType = repo.getAttribute(attributeNameYaxis).getDataType().getJavaType();
			
			logger.info("attributeXJavaType: " + attributeXJavaType + " attributeNameXaxis: " + attributeNameXaxis);
			logger.info("attributeYJavaType: " + attributeYJavaType + " attributeNameYaxis: " + attributeNameYaxis);
			
			XYDataSerie xYDataSerie = this.getXYDataSerie(entityName, attributeNameXaxis, attributeNameYaxis, attributeXJavaType, attributeYJavaType, queryRules);
			XYDataChart xYDataChart = new XYDataChart(Arrays.asList(xYDataSerie), getMolgenisAxisType(attributeXJavaType), getMolgenisAxisType(attributeYJavaType));
			return xYDataChart;
		}
		catch (MolgenisModelException e)
		{
			throw new MolgenisChartException("Error creating a line chart, error: " + e);
		}
	}
	
	
	@Override
	public XYDataSerie getXYDataSerie(
			String entityName, 
			String attributeNameXaxis, 
			String attributeNameYaxis,
			Class<?> attributeXJavaType,
			Class<?> attributeYJavaType,
			List<QueryRule> queryRules)
	{		
		Repository<? extends Entity> repo = dataService.getRepositoryByEntityName(entityName);
		
		XYDataSerie serie = new XYDataSerie();
		serie.setName(
				repo.getAttribute(attributeNameXaxis).getLabel() + 
				" vs " + 
				repo.getAttribute(attributeNameYaxis).getLabel());
		serie.setAttributeXJavaType(attributeXJavaType);
		serie.setAttributeYJavaType(attributeYJavaType);

		if (repo instanceof Queryable)
		{
			if (queryRules == null)
			{
				queryRules = new ArrayList<QueryRule>();
			}
			
			logger.info("queryRules: " + queryRules);
			
			Query q = new QueryImpl(queryRules);
			Sort sort = new Sort(Sort.DEFAULT_DIRECTION, attributeNameXaxis, attributeNameYaxis);
			q.sort(sort);

			Iterable<? extends Entity> iterable = ((Queryable<? extends Entity>) repo).findAll(q);

			for (Entity entity : iterable)
			{
				Object x = getJavaEntityValue(entity, attributeNameXaxis, attributeXJavaType);
				Object y = getJavaEntityValue(entity, attributeNameYaxis, attributeYJavaType);

				serie.addData(new XYData(x, y));
			}
			
			logger.info("repo instanceof Queryable");

		}
		else
		{
			// TODO JJ
			throw new MolgenisChartException("entity: " + entityName + " is not queryable and is not supported");

			// TODO JJ fix me
			// Excel documents are not supported because they are not Queryable
			// if (queryRules != null && !queryRules.isEmpty()) {
			// throw new MolgenisChartException("There a query rules defined but the " + entityName
			// + " repository is not queryable");
			// }
			//
			// for (Entity entity : repo)
			// {
			// Object x = entity.get(attributeNameXaxis);
			// Object y = entity.get(attributeNameYaxis);
			//
			// serie.addData(new XYData(x, y));
			// }
		}

		try
		{
			logger.info("Type: " + repo.getAttribute(attributeNameXaxis).getDataType().getJavaType());
		}
		catch (MolgenisModelException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("serie.getData().size(): " + serie.getData().size());

		return serie;
	}

	public Object getJavaEntityValue(Entity entity, String attributeName, Class<?> attributeJavaType)
	{	
		if(Double.class == attributeJavaType)
		{
			return entity.getDouble(attributeName);
		} 
		else if (Date.class == attributeJavaType) 
		{
			return entity.getDate(attributeName);
		}
		else if (String.class == attributeJavaType) 
		{
			return entity.getString(attributeName);
		} 
		else if (Timestamp.class == attributeJavaType) 
		{
			return entity.getTimestamp(attributeName);
		}
		else {
			return null;
		}
	}
	
	public MolgenisAxisType getMolgenisAxisType(Class<?> attributeJavaType)
	{	
		if(Double.class == attributeJavaType)
		{
			return MolgenisAxisType.LINEAR;
		} 
		else if (Date.class == attributeJavaType) 
		{
			return MolgenisAxisType.DATETIME;
		}
		else if (String.class == attributeJavaType) 
		{
			return MolgenisAxisType.CATEGORY;
		} 
		else if (Timestamp.class == attributeJavaType) 
		{
			return MolgenisAxisType.DATETIME;
		}
		else {
			return null;
		}
	}
	
	public Class<?> getAttributeJavaType(String entityName, String attributeName)
	{
		Repository<? extends Entity> repo = dataService.getRepositoryByEntityName(entityName);
		try
		{
			return repo.getAttribute(attributeName).getDataType().getJavaType();
		}
		catch (MolgenisModelException e)
		{
			e.printStackTrace();
			return null;
		}
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
