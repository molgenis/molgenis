package org.molgenis.dataSetAnnotators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
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
 * @param DataService
 * @param DataSetsIndexer
 * @param RepositoryAnnotator
 * @param Repository
 * @param Boolean
 *            createCopy
 * @return Repository
 * 
 * */
public class OmxDataSetAnnotator
{
	DataService dataService;
	DataSetsIndexer indexer;

	public OmxDataSetAnnotator(DataService dataService, DataSetsIndexer indexer)
	{
		//TODO Make this more spring-like?
		this.dataService = dataService;
		this.indexer = indexer;
	}

	@Transactional
	public void annotate(RepositoryAnnotator annotator, Repository repo, boolean createCopy)
	{
		// retrieve the data set that sits within the repo passed down from the controller
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, repo.getName()), DataSet.class);

		// add new protocol to store the results of the specified annotator
		Protocol resultProtocol = new Protocol();
		resultProtocol.setIdentifier(annotator.getClass().getName() + UUID.randomUUID());
		resultProtocol.setName(annotator.getClass().getName());
		dataService.add(Protocol.ENTITY_NAME, resultProtocol);

		// create a list with feature names that are required to call the specified annotator
		List<String> inputFeatureNames = new ArrayList<String>();
		Iterator<AttributeMetaData> inputIterator = annotator.getInputMetaData().getAttributes().iterator();
		
		while (inputIterator.hasNext())
		{
			AttributeMetaData attributeMetaData = (AttributeMetaData) inputIterator.next();
			inputFeatureNames.add(attributeMetaData.getName());
		}
		
		// create a list with all the feature names that are returned by the specified annotator
		List<String> featureNames = new ArrayList<String>();
		Iterator<AttributeMetaData> outputIterator = annotator.getOutputMetaData().getAttributes().iterator();
		
		while (outputIterator.hasNext())
		{
			AttributeMetaData attributeMetaData = (AttributeMetaData) outputIterator.next();
			featureNames.add(attributeMetaData.getName());
		}
		
		// create features to store the results of the specified annotator
		for (String name : featureNames)
		{
			ObservableFeature newFeature = new ObservableFeature();
			newFeature.setIdentifier(name + "_id");
			newFeature.setName(name);
			dataService.add(ObservableFeature.ENTITY_NAME, newFeature);

			resultProtocol.getFeatures().add(newFeature);
		}

		// update the protocol with a protocol holding the new observable features 
		dataService.update(Protocol.ENTITY_NAME, resultProtocol);

		// add resultProtocol to the protocol_used of the data set
		Protocol repositoryProtocol = dataService.findOne(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.IDENTIFIER, dataSet.getProtocolUsed().getIdentifier()), Protocol.class);

		repositoryProtocol.getSubprotocols().add(resultProtocol);
		dataService.update(Protocol.ENTITY_NAME, repositoryProtocol);

		// retrieve an iterator over observation sets for the repository data set
		Iterable<ObservationSet> osSet = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);

		// call the specified annotation service with the specified repo, this returns
		// an iterator over the results given by said annotation service
		Iterator<Entity> entityIterator = annotator.annotate(repo.iterator());
		
		// create a CrudRepository so that a set knows what features it has
		CrudRepository valueRepo = (CrudRepository) dataService.getRepositoryByEntityName(ObservedValue.ENTITY_NAME);

		// FIXME what to do in case of multiple input attributes
		String inputFeatureName = inputFeatureNames.get(0);
		
		// specify what observable feature is already in the data set, and which is used to call
		// the specified annotation service
		ObservableFeature inputFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, inputFeatureName), ObservableFeature.class);

		// iterate over the annotation service results
		while (entityIterator.hasNext())
		{
			Entity entity = entityIterator.next();
			
			// for every set
			for (ObservationSet os : osSet)
			{
				// retrieve a value from this observation set based on a specified feature
				ObservedValue value = valueRepo.findOne(
						new QueryImpl().eq(ObservedValue.OBSERVATIONSET, os).eq(ObservedValue.FEATURE, inputFeature),
						ObservedValue.class);

				// retrieve its feature name
				String inputValue = value.getValue().getString("value");

				// if the before specified required column name equals that of the retrieved value
				if (entity.get(inputFeatureName).equals(inputValue))
				{
					for (String columnName : featureNames)
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
				}
			}
		}

		// after adding annotation to the existing OMX data set,
		// run the indexer so the data explorer auto updates
		ArrayList<Integer> datasetIds = new ArrayList<Integer>();
		datasetIds.add(dataSet.getId());
		indexer.indexDataSets(datasetIds);
	}
}