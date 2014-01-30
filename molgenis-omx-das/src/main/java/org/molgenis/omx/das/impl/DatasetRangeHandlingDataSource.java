package org.molgenis.omx.das.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.das.RangeHandlingDataSource;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.util.ApplicationContextProvider;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasType;

public class DatasetRangeHandlingDataSource extends RangeHandlingDataSource implements
		RangeHandlingAnnotationDataSource
{
	private final DataService dataService;
	private DasType mutationType;
	private DasMethod method;
	private String type;

	@Override
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig) throws DataSourceException
	{
		this.type = dataSourceConfig.getDataSourceProperties().get("type").getValue();
		this.mutationType = new DasType(type, null, "?", type);
		this.method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");

	}

	// for unit test
	DatasetRangeHandlingDataSource(DataService dataService, DasType mutationType, DasMethod method)
			throws DataSourceException
	{
		this.dataService = dataService;
		this.type = "type";
		this.mutationType = mutationType;
		this.method = method;
	}

	public DatasetRangeHandlingDataSource() throws DataSourceException
	{
		dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);
	}

	@Override
	public DasAnnotatedSegment getFeatures(String segmentParamString, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException
	{

		String[] segmentParts = segmentParamString.split(",");
		DataSet dataSet = null;
		String customParam;
		String segmentId = null;
		if (segmentParts.length > 1)
		{
			segmentId = segmentParts[0];
			customParam = segmentParts[1];
			if (customParam.indexOf("dataset_") != -1)
			{
				dataSet = findDataSet(customParam.substring(8));
			}
		}

		Map<ObservationSet, Iterable<ObservedValue>> valueMap = queryDataSet(segmentId, start, stop, dataSet);
		List<DasFeature> features = new ArrayList<DasFeature>();

		Iterator it = valueMap.entrySet().iterator();
		while (it.hasNext())
		{
			DasFeature feature;
			Map.Entry pairs = (Map.Entry) it.next();
			List<ObservedValue> ValueList = (List<ObservedValue>) pairs.getValue();
			Integer valueStart = null;
			Integer valueStop = null;
			String valueDescription = null;
			String valueIdentifier = null;
			String valueName = null;
			String valueLink = null;
			for (ObservedValue value : ValueList)
			{
				if (MUTATION_START_POSITION.equals(value.getFeature().getIdentifier()))
				{
					valueStart = value.getValue().getInt("value");
				}
				else if (MUTATION_STOP_POSITION.equals(value.getFeature().getIdentifier()))
				{
					valueStop = value.getValue().getInt("value");
				}
				else if (MUTATION_DESCRIPTION.equals(value.getFeature().getIdentifier()))
				{
					valueDescription = value.getValue().getString("value");
				}
				else if (MUTATION_ID.equals(value.getFeature().getIdentifier()))
				{
					valueIdentifier = value.getValue().getString("value");
				}
				else if (MUTATION_NAME.equals(value.getFeature().getIdentifier()))
				{
					valueName = value.getValue().getString("value");
				}
				else if (MUTATION_LINK.equals(value.getFeature().getIdentifier()))
				{
					valueLink = value.getValue().getString("value");
				}
			}
			if (valueStart != null && valueIdentifier != null)
			{
				feature = createDasFeature(valueStart, valueStop, valueIdentifier, valueName, valueDescription,
						valueLink, mutationType, method);
				features.add(feature);
			}
		}

		DasAnnotatedSegment segment = new DasAnnotatedSegment(segmentId, start, stop, "1.00", segmentId, features);
		return segment;

	}

	protected DataSet findDataSet(String dataSet)
	{
		return dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl().eq(DataSet.IDENTIFIER, dataSet), DataSet.class);
	}

	protected Map<ObservationSet, Iterable<ObservedValue>> queryDataSet(String segmentId, int start, int stop,
			DataSet dataSet)
	{
		Iterable<ObservationSet> observationSets;
		Map<ObservationSet, Iterable<ObservedValue>> results = new HashMap<ObservationSet, Iterable<ObservedValue>>();
		Iterable<ObservedValue> values;

		QueryImpl variantQuery = new QueryImpl();
		variantQuery.eq(ObservationSet.PARTOFDATASET, dataSet);

		observationSets = dataService.findAll(ObservationSet.ENTITY_NAME, variantQuery, ObservationSet.class);
		for (ObservationSet observationSet : observationSets)
		{
			values = dataService.findAll(ObservedValue.ENTITY_NAME,
					new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet), ObservedValue.class);
			Integer valueStart = null;
			Integer valueStop = null;
			String valueChromosome = null;
			for (ObservedValue value : values)
			{
				if (MUTATION_START_POSITION.equals(value.getFeature().getIdentifier()))
				{
					valueStart = value.getValue().getInt("value");
				}
				if (MUTATION_STOP_POSITION.equals(value.getFeature().getIdentifier()))
				{
					valueStop = value.getValue().getInt("value");
				}
				if (MUTATION_CHROMOSOME.equals(value.getFeature().getIdentifier()))
				{
					valueChromosome = value.getValue().getString("value");
				}
			}
			if (((valueStart > start && valueStart < stop) || valueStop != null
					&& (valueStop > start && valueStop < stop))
					&& valueChromosome.equals(segmentId))
			{
				results.put(observationSet, values);
			}
		}
		return results;
	}

	@Override
	public Integer getTotalCountForType(DasType type) throws DataSourceException
	{
		return -1;
	}

	@Override
	public Collection<DasType> getTypes() throws DataSourceException
	{
		return Collections.singleton(mutationType);
	}
}
