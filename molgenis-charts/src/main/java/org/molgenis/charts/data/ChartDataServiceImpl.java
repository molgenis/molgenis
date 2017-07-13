package org.molgenis.charts.data;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.charts.*;
import org.molgenis.charts.calculations.BoxPlotCalcUtil;
import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

@Component
public class ChartDataServiceImpl implements ChartDataService
{
	private final DataService dataService;

	@Autowired
	public ChartDataServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public XYDataChart getXYDataChart(String entityTypeId, String attributeNameXaxis, String attributeNameYaxis,
			String split, List<QueryRule> queryRules)
	{
		Repository<Entity> repo = dataService.getRepository(entityTypeId);
		EntityType entityType = repo.getEntityType();

		final AttributeType attributeXFieldTypeEnum = entityType.getAttribute(attributeNameXaxis).getDataType();
		final AttributeType attributeYFieldTypeEnum = entityType.getAttribute(attributeNameYaxis).getDataType();
		final List<XYDataSerie> xYDataSeries;

		// Sanity check
		if (AttributeType.DECIMAL.equals(attributeXFieldTypeEnum) || AttributeType.INT.equals(attributeXFieldTypeEnum)
				|| AttributeType.LONG.equals(attributeXFieldTypeEnum) || AttributeType.DATE.equals(
				attributeXFieldTypeEnum) || AttributeType.DATE_TIME.equals(attributeXFieldTypeEnum)
				|| AttributeType.DECIMAL.equals(attributeYFieldTypeEnum) || AttributeType.INT.equals(
				attributeYFieldTypeEnum) || AttributeType.LONG.equals(attributeYFieldTypeEnum)
				|| AttributeType.DATE.equals(attributeYFieldTypeEnum) || AttributeType.DATE_TIME.equals(
				attributeYFieldTypeEnum))
		{
			if (!StringUtils.isNotBlank(split))
			{
				xYDataSeries = Arrays.asList(
						this.getXYDataSerie(repo, entityTypeId, attributeNameXaxis, attributeNameYaxis,
								attributeXFieldTypeEnum, attributeYFieldTypeEnum, queryRules));
			}
			else
			{
				xYDataSeries = this.getXYDataSeries(repo, entityTypeId, attributeNameXaxis, attributeNameYaxis,
						attributeXFieldTypeEnum, attributeYFieldTypeEnum, split, queryRules);
			}

			return new XYDataChart(xYDataSeries, MolgenisAxisType.getType(attributeXFieldTypeEnum),
					MolgenisAxisType.getType(attributeYFieldTypeEnum));
		}
		else
		{
			throw new MolgenisChartException(
					"For the x and the y axis selected datatype are wrong. Scatterplots can only handle Continuous data");
		}
	}

	@Override
	public XYDataSerie getXYDataSerie(Repository<Entity> repo, String entityTypeId, String attributeNameXaxis,
			String attributeNameYaxis, AttributeType attributeXFieldTypeEnum, AttributeType attributeYFieldTypeEnum,
			List<QueryRule> queryRules)
	{
		EntityType entityType = repo.getEntityType();

		XYDataSerie serie = new XYDataSerie();
		serie.setName(entityType.getAttribute(attributeNameXaxis).getLabel() + " vs " + entityType.getAttribute(
				attributeNameYaxis).getLabel());
		serie.setAttributeXFieldTypeEnum(attributeXFieldTypeEnum);
		serie.setAttributeYFieldTypeEnum(attributeYFieldTypeEnum);

		Sort sort = new Sort().on(attributeNameXaxis).on(attributeNameYaxis);
		Iterable<? extends Entity> iterable = getIterable(entityTypeId, repo, queryRules, sort);
		for (Entity entity : iterable)
		{
			Object x = getJavaValue(entity, attributeNameXaxis, attributeXFieldTypeEnum);
			Object y = getJavaValue(entity, attributeNameYaxis, attributeYFieldTypeEnum);
			serie.addData(new XYData(x, y));
		}

		return serie;
	}

	@Override
	public List<XYDataSerie> getXYDataSeries(Repository<Entity> repo, String entityTypeId, String attributeNameXaxis,
			String attributeNameYaxis, AttributeType attributeXFieldTypeEnum, AttributeType attributeYFieldTypeEnum,
			String split, List<QueryRule> queryRules)

	{
		Sort sort = new Sort().on(attributeNameXaxis).on(attributeNameYaxis);
		Iterable<? extends Entity> iterable = getIterable(entityTypeId, repo, queryRules, sort);

		Map<String, XYDataSerie> xYDataSeriesMap = new HashMap<>();
		for (Entity entity : iterable)
		{
			String splitValue = createSplitKey(entity, split);
			if (!xYDataSeriesMap.containsKey(splitValue))
			{
				XYDataSerie serie = new XYDataSerie();
				serie.setName(splitValue);
				serie.setAttributeXFieldTypeEnum(attributeXFieldTypeEnum);
				serie.setAttributeYFieldTypeEnum(attributeYFieldTypeEnum);
				xYDataSeriesMap.put(splitValue, serie);
			}

			Object x = getJavaValue(entity, attributeNameXaxis, attributeXFieldTypeEnum);
			Object y = getJavaValue(entity, attributeNameYaxis, attributeYFieldTypeEnum);
			xYDataSeriesMap.get(splitValue).addData(new XYData(x, y));
		}

		List<XYDataSerie> series = new ArrayList<>();
		for (Entry<String, XYDataSerie> serie : xYDataSeriesMap.entrySet())
		{
			series.add(serie.getValue());
		}

		return series;
	}

	private String getValueAsString(Entity entity, String split)
	{
		Object o = entity.get(split);
		if (o instanceof Entity)
		{
			Object labelValue = ((Entity) o).getLabelValue();
			return labelValue != null ? labelValue.toString() : null;
		}
		else if (o instanceof List)
		{
			@SuppressWarnings("unchecked")
			Iterable<Object> refObjects = (Iterable<Object>) o;
			StringBuilder strBuilder = new StringBuilder();
			for (Object ob : refObjects)
			{
				if (strBuilder.length() > 0)
				{
					strBuilder.append(',');
				}

				if (ob instanceof Entity)
				{
					Object labelValue = ((Entity) ob).getLabelValue();
					strBuilder.append(labelValue != null ? labelValue.toString() : null);
				}
				else
				{
					strBuilder.append(ob.toString());
				}
			}
			return strBuilder.toString();
		}
		else
		{
			return "" + o;
		}
	}

	public String createSplitKey(Entity entity, String split)
	{
		return split + '(' + getValueAsString(entity, split) + ')';
	}

	@Override
	/**
	 * init a box plot chart
	 *
	 * with: 1. box plot series as the boxes 2. xy data series as the outliers 3. categories names of the different box
	 * plots
	 *
	 * @param boxPlotChart
	 *            (BoxPlotChart) the chart to be initialized
	 * @param repo
	 *            (Repository<? extends Entity>) the repository where the data exists
	 * @param entityTypeId
	 *            (String) the name of the entity to be used
	 * @param attributeName
	 *            (String) the name of the observable value in the entity
	 * @param queryRules
	 *            (List<QueryRule>) the query rules to be used getting the data from the repo
	 * @param split
	 *            (String) the name of observable value where the data needs to split on
	 * @param scaleToCalcOutliers
	 *            (double) the scale that need to be used calculating the outliers. value 1 means that there wil not be
	 *            any outliers.
	 */ public BoxPlotChart getBoxPlotChart(String entityTypeId, String attributeName, List<QueryRule> queryRules,
			String split, double scaleToCalcOutliers)
	{
		Repository<Entity> repo = dataService.getRepository(entityTypeId);
		EntityType entityType = repo.getEntityType();

		BoxPlotChart boxPlotChart = new BoxPlotChart();
		boxPlotChart.setyLabel(entityType.getAttribute(attributeName).getLabel());

		Sort sort = new Sort().on(attributeName);
		Iterable<Entity> iterable = getIterable(entityTypeId, repo, queryRules, sort);
		Map<String, List<Double>> boxPlotDataListMap = getBoxPlotDataListMap(entityType, iterable, attributeName,
				split);

		BoxPlotSerie boxPlotSerie = new BoxPlotSerie();
		boxPlotSerie.setType(MolgenisSerieType.BOXPLOT);
		boxPlotSerie.setName("Boxplot");

		XYDataSerie xYDataSerie = new XYDataSerie();
		xYDataSerie.setType(MolgenisSerieType.SCATTER);
		xYDataSerie.setName("Outliers");

		List<String> categories = new ArrayList<>();

		int count = 0;
		for (Entry<String, List<Double>> entry : boxPlotDataListMap.entrySet())
		{

			List<Double> list = entry.getValue();
			categories.add(entry.getKey());
			Double[] data = BoxPlotCalcUtil.calcBoxPlotValues(entry.getValue());
			double iqr = BoxPlotCalcUtil.iqr(data[3], data[1]);
			double step = iqr * scaleToCalcOutliers;
			double highBorder = step + data[3];
			double lowBorder = data[1] - step;

			List<XYData> outlierList = new ArrayList<>();
			List<Double> normalList = new ArrayList<>();
			for (Double o : list)
			{
				if (null != o)
				{
					if (o < lowBorder || o > highBorder)
					{
						outlierList.add(new XYData(count, o));
					}
					else
					{
						normalList.add(o);
					}
				}
			}

			xYDataSerie.addData(outlierList);
			data = BoxPlotCalcUtil.calcBoxPlotValues(normalList);
			boxPlotSerie.addData(data);
			count++;
		}

		boxPlotChart.addBoxPlotSerie(boxPlotSerie);
		boxPlotChart.addXYDataSerie(xYDataSerie);
		boxPlotChart.setCategories(categories);
		return boxPlotChart;
	}

	/**
	 * Get a map containing keys to different data lists
	 *
	 * @param entityType
	 * @param iterable
	 * @param attributeName
	 * @param split         (String) if null or empty String will not split
	 * @return map (Map<String, List<Double>>)
	 */
	private Map<String, List<Double>> getBoxPlotDataListMap(EntityType entityType, Iterable<Entity> iterable,
			String attributeName, String split)
	{
		Map<String, List<Double>> boxPlotDataListMap = new HashMap<>();
		final boolean splitList = StringUtils.isNotBlank(split);

		if (splitList)
		{
			for (Entity entity : iterable)
			{
				String key = createSplitKey(entity, split);
				if (!boxPlotDataListMap.containsKey(key))
				{
					boxPlotDataListMap.put(key, new ArrayList<>());
				}
				boxPlotDataListMap.get(key).add(entity.getDouble(attributeName));
			}
		}
		else
		{
			String key = entityType.getAttribute(attributeName).getLabel();
			List<Double> list = new ArrayList<>();
			for (Entity entity : iterable)
			{
				list.add(entity.getDouble(attributeName));
			}
			boxPlotDataListMap.put(key, list);
		}
		return boxPlotDataListMap;
	}

	/**
	 * get a Iterable holding entitys
	 *
	 * @param entityTypeId (String) the name of the entity to be used
	 * @param repo         (Repository<? extends Entity>) the repository where the data exists
	 * @param queryRules   (List<QueryRule>) the query rules to be used getting the data from the repo
	 * @param sort         (Sort)
	 * @return
	 */

	private Iterable<Entity> getIterable(String entityTypeId, Repository<Entity> repo, List<QueryRule> queryRules,
			Sort sort)
	{

		final Query<Entity> q;
		if (queryRules == null)
		{
			q = new QueryImpl<>();
		}
		else
		{
			q = new QueryImpl<>(queryRules);
		}

		if (null != sort)
		{
			q.sort(sort);
		}

		return () -> repo.findAll(q).iterator();
	}

	/**
	 * retrieves the value of the designated column as attributeJavaType
	 *
	 * @param entity
	 * @param attributeName
	 * @param attributeFieldTypeEnum
	 * @return value (Object)
	 */
	private Object getJavaValue(Entity entity, String attributeName, AttributeType attributeFieldTypeEnum)
	{
		if (AttributeType.DECIMAL.equals(attributeFieldTypeEnum))
		{
			return entity.getDouble(attributeName);
		}
		else if (AttributeType.INT.equals(attributeFieldTypeEnum))
		{
			return entity.getInt(attributeName);
		}
		else if (AttributeType.LONG.equals(attributeFieldTypeEnum))
		{
			return entity.getLong(attributeName);
		}
		else if (AttributeType.DATE_TIME.equals(attributeFieldTypeEnum))
		{
			return entity.getInstant(attributeName);
		}
		else if (AttributeType.DATE.equals(attributeFieldTypeEnum))
		{
			return entity.getLocalDate(attributeName);
		}
		else
		{
			return entity.getString(attributeName);
		}
	}

	@Override
	public DataMatrix getDataMatrix(String entityTypeId, List<String> attributeNamesXaxis, String attributeNameYaxis,
			List<QueryRule> queryRules)
	{
		Iterable<Entity> iterable = dataService.getRepository(entityTypeId);

		if (queryRules != null && !queryRules.isEmpty())
		{
			QueryImpl<Entity> q = new QueryImpl<>();
			for (QueryRule queryRule : queryRules)
			{
				q.addRule(queryRule);
			}

			final Iterable<Entity> AllEntitiesIterable = iterable;
			iterable = () -> ((Repository<Entity>) AllEntitiesIterable).findAll(q).iterator();
		}

		List<Target> rowTargets = new ArrayList<>();
		List<Target> columnTargets = new ArrayList<>();
		List<List<Number>> values = new ArrayList<>();

		for (String columnTargetName : attributeNamesXaxis)
		{
			columnTargets.add(new Target(columnTargetName));
		}

		for (Entity entity : iterable)
		{
			String rowTargetName =
					entity.getString(attributeNameYaxis) != null ? entity.getString(attributeNameYaxis) : "";
			rowTargets.add(new Target(rowTargetName));

			List<Number> rowValues = new ArrayList<>();
			for (String attr : attributeNamesXaxis)
			{
				rowValues.add(entity.getDouble(attr));
			}
			values.add(rowValues);
		}

		return new DataMatrix(columnTargets, rowTargets, values);
	}
}
