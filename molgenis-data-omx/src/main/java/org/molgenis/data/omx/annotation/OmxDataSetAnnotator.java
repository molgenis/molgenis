package org.molgenis.data.omx.annotation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.search.DataSetsIndexer;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * This class uses a repository and calls an annotation service to add features and values to an existing data set.
 * </p>
 * 
 * @authors mdehaan,bcharbon
 * 
 * */
public class OmxDataSetAnnotator
{
	// FIXME unit test this class!
	private static final Logger logger = Logger.getLogger(OmxDataSetAnnotator.class);

	private static final String PROTOCOL_SUFFIX = "_annotator_id";
	private final SearchService searchService;
	private final EntityValidator entityValidator;
	DataService dataService;
	DataSetsIndexer indexer;

	/**
	 * @param dataService
	 * @param searchService
	 * @param indexer
	 * 
	 * */
	public OmxDataSetAnnotator(DataService dataService, SearchService searchService, DataSetsIndexer indexer,
			EntityValidator entityValidator)
	{
		this.dataService = dataService;
		this.searchService = searchService;
		this.indexer = indexer;
		this.entityValidator = entityValidator;
	}

	/**
	 * helper function to use multiple annotators on a repository, if the createRepo boolean is true only a 1 copy of
	 * the set will be made and used for all annotators
	 * 
	 * @param annotators
	 * @param repo
	 * @param createCopy
	 */
	public void annotate(List<RepositoryAnnotator> annotators, Repository repo, boolean createCopy)
	{
		for (RepositoryAnnotator annotator : annotators)
		{
			repo = annotate(annotator, repo, createCopy);
			createCopy = false;
		}
	}

	/**
	 * <p>
	 * This method calls an annotation service and creates an entity of annotations based on one or more columns. Input
	 * and output meta data is collected for the creating Observable features. The protocol holding the added features
	 * from the annotator and the annotation values are then added to the data set of the supplied repository.
	 * </p>
	 * <p>
	 * The resulting annotated data set is then indexed so users or admins do not have to re-index manually.
	 * </p>
	 * 
	 * @param annotator
	 * @param repo
	 * @param createCopy
	 * 
	 * */
	@Transactional
	public Repository annotate(RepositoryAnnotator annotator, Repository repo, boolean createCopy)
	{
		Iterator<Entity> entityIterator = annotator.annotate(repo.iterator());
		List<String> inputMetadataNames = getMetadataNamesAsList(annotator.getInputMetaData());
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, repo.getName()), DataSet.class);
		if (createCopy)
		{
			dataSet = copyDataSet(dataSet);
		}

		Protocol resultProtocol = dataService.findOne(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.IDENTIFIER, annotator.getName() + PROTOCOL_SUFFIX), Protocol.class);
		if (resultProtocol == null)
		{
			resultProtocol = createAnnotationResultProtocol(annotator, dataSet, annotator.getOutputMetaData()
					.getAttributes());
		}
		if (!dataSet.getProtocolUsed().getSubprotocols().contains(resultProtocol))
		{
			addAnnotationResultProtocol(dataSet, resultProtocol);
		}
		addAnnotationResults(inputMetadataNames, getMetadataNamesAsList(annotator.getOutputMetaData()), dataSet,
				entityIterator, annotator);

		indexResultDataSet(dataSet);

		return dataService.getRepositoryByEntityName(dataSet.getIdentifier());
	}

	private void addAnnotationResults(List<String> inputMetadataNames, List<String> outputMetadataNames,

	DataSet dataSet, Iterator<Entity> annotationResultIterator, RepositoryAnnotator annotator)

	{
		List<String> processedObservationSets = new ArrayList<String>();
		Iterable<ObservationSet> ObservationSets = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);

		CrudRepository valueRepo = (CrudRepository) dataService.getRepositoryByEntityName(ObservedValue.ENTITY_NAME);
		Map<String, Object> inputValues;
		while (annotationResultIterator.hasNext())
		{
			Entity entity = annotationResultIterator.next();
			for (ObservationSet observationSet : ObservationSets)
			{
				inputValues = createInputValueMap(inputMetadataNames, valueRepo, observationSet);
				if (inputValues.size() != 0)
				{
					if (entityEqualsObservationSet(entity, inputValues, inputMetadataNames))
					{

						if (!processedObservationSets.contains(observationSet.getIdentifier()))
						{
							processedObservationSets.add(observationSet.getIdentifier());
						}
						else
						{
							copyObservationSets(Collections.singletonList(observationSet), null);
							processedObservationSets.add(observationSet.getIdentifier());
						}
						for (String columnName : outputMetadataNames)
						{
							try
							{
								addValue(entity, observationSet, columnName, annotator.getName());
							}
							catch (Exception e)
							{
								logger.error(e.getMessage());
							}
						}

					}
				}
			}
		}
	}

	private void indexResultDataSet(DataSet dataSet)
	{
		ArrayList<Object> datasetIds = new ArrayList<Object>();
		datasetIds.add(dataSet.getId());
		indexer.indexDataSets(datasetIds);
	}

	private Protocol createAnnotationResultProtocol(RepositoryAnnotator annotator, DataSet dataSet,
			Iterable<AttributeMetaData> outputMetadataNames)
	{

		Protocol resultProtocol = new Protocol();
		resultProtocol.setIdentifier(annotator.getName() + PROTOCOL_SUFFIX);
		resultProtocol.setName(annotator.getName());
		dataService.add(Protocol.ENTITY_NAME, resultProtocol);
		addOutputFeatures(resultProtocol, outputMetadataNames, annotator.getName());
		return resultProtocol;
	}

	private void addAnnotationResultProtocol(DataSet dataSet, Protocol resultProtocol)
	{
		Protocol rootProtocol = dataService.findOne(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.IDENTIFIER, dataSet.getProtocolUsed().getIdentifier()), Protocol.class);

		rootProtocol.getSubprotocols().add(resultProtocol);
		dataService.update(Protocol.ENTITY_NAME, rootProtocol);
	}

	private List<String> getMetadataNamesAsList(EntityMetaData metadata)
	{
		Iterator<AttributeMetaData> metadataIterator = metadata.getAttributes().iterator();

		List<String> inputFeatureNames = new ArrayList<String>();
		while (metadataIterator.hasNext())
		{
			AttributeMetaData attributeMetaData = metadataIterator.next();
			inputFeatureNames.add(attributeMetaData.getName());
		}
		return inputFeatureNames;
	}

	private void addOutputFeatures(Protocol resultProtocol, Iterable<AttributeMetaData> metaData, String prefix)
	{
		for (AttributeMetaData attributeMetaData : metaData)
		{
			ObservableFeature newFeature = new ObservableFeature();
			String newFeatureName = prefix + attributeMetaData.getName();
			if (dataService.findOne(ObservableFeature.ENTITY_NAME,
					new QueryImpl().eq(ObservableFeature.IDENTIFIER, newFeatureName)) == null)
			{
				newFeature.setIdentifier(newFeatureName);
				newFeature.setName(attributeMetaData.getLabel());
				newFeature.setDataType(attributeMetaData.getDataType().toString());
				dataService.add(ObservableFeature.ENTITY_NAME, newFeature);
				resultProtocol.getFeatures().add(newFeature);
			}
			dataService.update(Protocol.ENTITY_NAME, resultProtocol);
		}
	}

	private void addValue(Entity entity, ObservationSet observationSet, String columnName, String prefix)
			throws ValueConverterException

	{
		ObservableFeature thisFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, prefix + columnName), ObservableFeature.class);
		ValueConverter valueConverter = new ValueConverter(dataService);
		Value value = valueConverter.fromEntity(entity, columnName, thisFeature);
		dataService.add(Value.ENTITY_NAME, value);

		ObservedValue observedValue = new ObservedValue();
		observedValue.setFeature(thisFeature);
		observedValue.setObservationSet(observationSet);
		observedValue.setValue(value);
		dataService.add(ObservedValue.ENTITY_NAME, observedValue);
	}

	private Map<String, Object> createInputValueMap(List<String> inputFeatureNames, CrudRepository valueRepo,
			ObservationSet observationSet)
	{
		Map<String, Object> inputValueMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		for (String inputFeatureName : inputFeatureNames)
		{
			ObservableFeature inputFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
					new QueryImpl().eq(ObservableFeature.IDENTIFIER, inputFeatureName), ObservableFeature.class);

			// retrieve a value from this observation set based on a specified feature
			ObservedValue value = valueRepo.findOne(new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet)
					.eq(ObservedValue.FEATURE, inputFeature), ObservedValue.class);

			if (value != null)
			{
				Value omxValue = value.getValue();
				if (omxValue != null)
				{
					Object inputValue = omxValue.get("value");
					inputValueMap.put(inputFeature.getIdentifier(), inputValue);
				}
			}

		}
		return inputValueMap;
	}

	private boolean entityEqualsObservationSet(Entity entity, Map<String, Object> inputValues,
			List<String> inputFeatureNames)
	{
		boolean areEqual = true;
		for (String inputFeatureName : inputFeatureNames)
		{
			if (!inputValues.get(inputFeatureName).equals(entity.get(inputFeatureName)))
			{
				areEqual = false;
				break;
			}
		}
		return areEqual;
	}

	@Transactional
	public DataSet copyDataSet(DataSet original)
	{
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy_HH:mm");
		String dateString = simpleDateFormat.format(date);

		Protocol newRootProtocol = new Protocol();
		newRootProtocol.setIdentifier(original.getProtocolUsed().getIdentifier() + calendar.getTimeInMillis());
		newRootProtocol.setName(original.getName() + "_results_" + dateString);

		List<Protocol> subprotocols = new ArrayList<Protocol>();
		subprotocols.add(original.getProtocolUsed());
		newRootProtocol.setSubprotocols(subprotocols);
		dataService.add(Protocol.ENTITY_NAME, newRootProtocol);

		DataSet copy = new DataSet();
		copy.setProtocolUsed(newRootProtocol);
		copy.setName(original.getName() + "_results_" + dateString);
		copy.setIdentifier(original.getIdentifier() + calendar.getTimeInMillis());
		copy.setStartTime(date);
		copy.setEndTime(date);

		Iterable<ObservationSet> observationSets = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, original), ObservationSet.class);
		dataService.add(DataSet.ENTITY_NAME, copy);
		OmxRepository repo = new OmxRepository(dataService, searchService, copy.getIdentifier(), entityValidator);
		dataService.addRepository(repo);

		copyObservationSets(observationSets, copy);

		return copy;
	}

	private void copyObservationSets(Iterable<ObservationSet> observationSets, DataSet copy)
	{
		List<ObservationSet> newObservationSets = new ArrayList<ObservationSet>();
		List<ObservedValue> newObservedValues = new ArrayList<ObservedValue>();
		for (ObservationSet observationSet : observationSets)
		{

			Iterable<ObservedValue> observedValues = dataService.findAll(ObservedValue.ENTITY_NAME,
					new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet), ObservedValue.class);
			if (copy != null)
			{
				observationSet.setPartOfDataSet(copy);
			}
			observationSet.setIdentifier(UUID.randomUUID().toString());
			observationSet.setId(null);
			newObservationSets.add(observationSet);
			for (ObservedValue observedValue : observedValues)
			{
				observedValue.setObservationSet(observationSet);
				observedValue.setId(null);
				newObservedValues.add(observedValue);
			}
		}

		dataService.add(ObservationSet.ENTITY_NAME, newObservationSets);
		dataService.add(ObservedValue.ENTITY_NAME, newObservedValues);
	}
}