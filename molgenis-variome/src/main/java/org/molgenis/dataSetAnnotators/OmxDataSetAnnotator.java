package org.molgenis.dataSetAnnotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.omx.search.DataSetsIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.ast.Indexer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * This class uses a repository and calls
 * </p>
 * 
 * @author mdehaan
 * 
 * */
public class OmxDataSetAnnotator
{
	DataService dataService;
	DataSetsIndexer indexer;

	/**
	 * <p>
	 * This constructor...
	 * </p>
	 * 
	 * @param DataService
	 * @param DataSetsIndexer
	 * 
	 * */
	public OmxDataSetAnnotator(DataService dataService, DataSetsIndexer indexer)
	{
		// TODO Make this more spring-like?
		this.dataService = dataService;
		this.indexer = indexer;
	}

	/**
	 * <p>
	 * This method...
	 * </p>
	 * 
	 * @param RepositoryAnnotator
	 * @param Repository
	 * @param Boolean
	 *            createCopy
	 * 
	 * */
	@Transactional
	public void annotate(RepositoryAnnotator annotator, Repository repo, boolean createCopy)
	{

		Iterator<Entity> entityIterator = annotator.annotate(repo.iterator());
		
		List<String> inputMetadataNames = getMetadataNamesAsList(annotator.getInputMetaData());
		List<String> outputMetadataNames = getMetadataNamesAsList(annotator.getOutputMetaData());
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, repo.getName()), DataSet.class);
		addAnnotationResultProtocol(annotator, dataSet, outputMetadataNames);
		addAnnotationResults(inputMetadataNames, outputMetadataNames, dataSet, entityIterator);
		indexResultDataSet(dataSet);
	}

	private void addAnnotationResults(List<String> inputMetadataNames, List<String> outputMetadataNames,
			DataSet dataSet, Iterator<Entity> entityIterator)
	{
		Iterable<ObservationSet> osSet = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);

		CrudRepository valueRepo = (CrudRepository) dataService.getRepositoryByEntityName(ObservedValue.ENTITY_NAME);
		Map<String, Object> inputValues = new HashMap<String, Object>();
		while (entityIterator.hasNext())
		{
			Entity entity = entityIterator.next();
			for (ObservationSet os : osSet)
			{
				inputValues = createInputValueMap(inputMetadataNames, valueRepo, os);	

				if (entityEqualsObservationSet(entity,inputValues,inputMetadataNames))							
				{
					for (String columnName : outputMetadataNames)
					{
						addValue(entity, os, columnName);
					}
				}
			}		
		}
	}

	private void indexResultDataSet(DataSet dataSet)
	{
		ArrayList<Integer> datasetIds = new ArrayList<Integer>();
		datasetIds.add(dataSet.getId());
		indexer.indexDataSets(datasetIds);
	}

	private void addAnnotationResultProtocol(RepositoryAnnotator annotator, DataSet dataSet,
			List<String> outputMetadataNames)
	{
		Protocol resultProtocol = new Protocol();
		
		resultProtocol.setIdentifier(annotator.getClass().getName() + UUID.randomUUID());
		resultProtocol.setName(annotator.getClass().getName());
		dataService.add(Protocol.ENTITY_NAME, resultProtocol);

		addOutputFeatures(resultProtocol, outputMetadataNames);
		dataService.update(Protocol.ENTITY_NAME, resultProtocol);

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
			AttributeMetaData attributeMetaData = (AttributeMetaData) metadataIterator.next();
			inputFeatureNames.add(attributeMetaData.getName());
		}
		return inputFeatureNames;
	}

	private void addOutputFeatures(Protocol resultProtocol, List<String> featureNames)
	{
		for (String name : featureNames)
		{
			ObservableFeature newFeature = new ObservableFeature();
			if(dataService.findOne(ObservableFeature.ENTITY_NAME, new QueryImpl().eq(ObservableFeature.IDENTIFIER, name + "_id")) == null){
				newFeature.setIdentifier(name + "_id");
				newFeature.setName(name);
				dataService.add(ObservableFeature.ENTITY_NAME, newFeature);
				
				resultProtocol.getFeatures().add(newFeature);
			}
		}
	}

	private void addValue(Entity entity, ObservationSet os, String columnName)
	{
		StringValue sv = new StringValue();
		sv.setValue(entity.get(columnName).toString());
		dataService.add(StringValue.ENTITY_NAME, sv);

		ObservedValue ov = new ObservedValue();

		ObservableFeature thisFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, columnName + "_id"),
				ObservableFeature.class);

		ov.setFeature(thisFeature);
		ov.setObservationSet(os);
		ov.setValue(sv);
		dataService.add(ObservedValue.ENTITY_NAME, ov);
	}

	private Map<String, Object> createInputValueMap(List<String> inputFeatureNames, CrudRepository valueRepo,
			ObservationSet os)
	{
		Map<String, Object> inputValueMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		for (String inputFeatureName : inputFeatureNames)
		{
			ObservableFeature inputFeature = dataService
					.findOne(ObservableFeature.ENTITY_NAME,
							new QueryImpl().eq(ObservableFeature.IDENTIFIER, inputFeatureName),
							ObservableFeature.class);

			// retrieve a value from this observation set based on a specified feature
			ObservedValue value = valueRepo.findOne(
					new QueryImpl().eq(ObservedValue.OBSERVATIONSET, os)
							.eq(ObservedValue.FEATURE, inputFeature), ObservedValue.class);
			String inputValue = value.getValue().getString("value");
			inputValueMap.put(inputFeature.getIdentifier(), inputValue);
		}
		return inputValueMap;
	}

	private boolean entityEqualsObservationSet(Entity entity, Map<String, Object> inputValues, List<String> inputFeatureNames)
	{
		boolean areEqual=true;
		for (String inputFeatureName : inputFeatureNames)
		{
			if(!entity.get(inputFeatureName).equals(inputValues.get(inputFeatureName))){
				areEqual = false;
				break;
			}
		}		
		return areEqual;
	}
}