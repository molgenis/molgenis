package org.molgenis.omx.datasetdeleter;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.protocol.OmxLookupTableEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service
public class DataSetDeleterServiceImpl implements DataSetDeleterService
{
	private final DataService dataService;
	private final SearchService searchService;

	@Autowired
	public DataSetDeleterServiceImpl(DataService dataService, SearchService searchService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (searchService == null) throw new IllegalArgumentException("Search service is null");
		this.dataService = dataService;
		this.searchService = searchService;
	}

	@Override
	@Transactional
	public String deleteData(String dataSetIdentifier, boolean deleteMetadata)
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);

		final String datasetName = dataSet.getName();

		deleteData(dataSet);
		searchService.deleteDocumentsByType(dataSetIdentifier);

		if (deleteMetadata)
		{
			Protocol protocolUsed = dataSet.getProtocolUsed();
			dataService.delete(DataSet.ENTITY_NAME, dataSet);
			deleteProtocol(protocolUsed);
			dataService.removeRepository(dataSetIdentifier);
		}

		return datasetName;
	}

	/**
	 * Deletes the data from a given dataSet
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            DataSet from which the data should be deleted
	 */
	void deleteData(DataSet dataset)
	{
		List<ObservationSet> observationSets = Lists.newArrayList(dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataset), ObservationSet.class));

		deleteDataObservationSets(observationSets);
	}

	/**
	 * 
	 * Deletes the observationSets
	 * 
	 * Note: package-private for testability
	 * 
	 * @param observationSets
	 *            the observationSets that should be deleted
	 */
	void deleteDataObservationSets(List<ObservationSet> observationSets)
	{
		for (ObservationSet observationSet : observationSets)
		{
			final List<ObservedValue> observedValues = Lists.newArrayList(dataService.findAll(
					ObservedValue.ENTITY_NAME, new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet),
					ObservedValue.class));

			deleteDataObservedValues(observedValues);
			dataService.delete(ObservationSet.ENTITY_NAME, observationSet);
		}
	}

	/**
	 * Deletes the observedValues
	 * 
	 * Note: package-private for testability
	 * 
	 * @param observedValues
	 *            the observedValues that should be deleted
	 */
	void deleteDataObservedValues(List<ObservedValue> observedValues)
	{
		List<Value> values = new ArrayList<Value>();

		// Optimisation
		final int maxDeleteBatch = 20;
		while (!observedValues.isEmpty())
		{
			List<ObservedValue> observedValuesToDelete = new ArrayList<ObservedValue>();
			for (int i = 0; (i < observedValues.size()) && (i < maxDeleteBatch); i++)
			{
				ObservedValue observedValue = observedValues.get(i);
				observedValuesToDelete.add(observedValue);
				values.add(observedValue.getValue());
			}
			dataService.delete(ObservedValue.ENTITY_NAME, observedValuesToDelete);
			observedValues.removeAll(observedValuesToDelete);
			observedValuesToDelete.clear();
		}

		deleteDataValues(values);
	}

	/**
	 * 
	 * Deletes the values
	 * 
	 * Note: package-private for testability
	 * 
	 * @param values
	 *            the values that should be deleted
	 */
	void deleteDataValues(List<Value> values)
	{
		// Optimisation
		final int maxDeleteBatch = 20;
		while (!values.isEmpty())
		{
			List<Value> valuesToDelete = new ArrayList<Value>();
			for (int i = 0; (i < values.size()) && (i < maxDeleteBatch); i++)
			{
				Value value = values.get(i);
				valuesToDelete.add(value);
			}
			dataService.delete(Value.ENTITY_NAME, valuesToDelete);
			values.removeAll(valuesToDelete);
			valuesToDelete.clear();
		}
	}

	/**
	 * Deletes all subprotocols which do not have multiple Protocols referencing them
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            protocols that should be deleted
	 */
	void deleteProtocol(Protocol protocol, List<Entity> allEntitiesList)
	{
		List<Protocol> subprotocolsToDelete = protocol.getSubprotocols();
		for (Protocol subprotocol : subprotocolsToDelete)
		{
			int superprotocolcount = countReferringEntities(subprotocol, allEntitiesList);
			if (superprotocolcount <= 1)
			{
				deleteProtocol(subprotocol, allEntitiesList);
			}
		}
		List<ObservableFeature> subFeatures = protocol.getFeatures();
		deleteFeatures(subFeatures, allEntitiesList);
		dataService.delete(Protocol.ENTITY_NAME, protocol);
	}

	/**
	 * Deletes a protocol and all his subprotocols which do not have multiple Protocols referencing them
	 * 
	 * Note: package-private for testability
	 * 
	 * @param protocol
	 */
	void deleteProtocol(Protocol protocol)
	{

		List<Entity> allEntitiesList = Lists.newArrayList(dataService.findAll(ObservedValue.ENTITY_NAME,
				new QueryImpl()));
		allEntitiesList.addAll(Lists.newArrayList(dataService.findAll(Protocol.ENTITY_NAME, new QueryImpl())));
		allEntitiesList.addAll(Lists.newArrayList(dataService.findAll(DataSet.ENTITY_NAME, new QueryImpl())));
		deleteProtocol(protocol, allEntitiesList);
	}

	/**
	 * Deletes all features which do not have multiple Protocols referencing them
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            features that should be deleted
	 */
	void deleteFeatures(List<ObservableFeature> features, List<Entity> allEntitiesList)
	{
		List<ObservableFeature> removableFeatures = new ArrayList<ObservableFeature>();

		for (ObservableFeature feature : features)
		{
			int entityCount = countReferringEntities(feature, allEntitiesList);
			if (entityCount <= 1)
			{
				removableFeatures.add(feature);
			}
		}

		for (ObservableFeature feature : removableFeatures)
		{
			String featureDataType = feature.getDataType();
			if ("categorical".equals(featureDataType))
			{
				Iterable<Category> categories = dataService.findAll(Category.ENTITY_NAME,
						new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature), Category.class);
				deleteCategories(categories);
			}

			// Remove repository lookup table if exists
			final String entityName = feature.getIdentifier();
			final String entityNameOmxLookupTable = OmxLookupTableEntityMetaData
					.createOmxLookupTableEntityMetaDataName(entityName);
			if (dataService.hasRepository(entityNameOmxLookupTable))
			{
				dataService.removeRepository(entityNameOmxLookupTable);
			}

			// Remove characteristic
			Characteristic c = dataService.findOne(Characteristic.ENTITY_NAME,
					new QueryImpl().eq(Characteristic.IDENTIFIER, entityName), Characteristic.class);
			dataService.delete(Characteristic.ENTITY_NAME, c);

			// Remove feature
			dataService.delete(ObservableFeature.ENTITY_NAME, feature);
		}
	}

	/**
	 * Deletes all ontologyTerms
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            features that should be deleted
	 */
	void deleteOntologyTerms(List<OntologyTerm> ontologyTerms)
	{
		for (OntologyTerm ontologyTerm : ontologyTerms)
		{
			dataService.delete(OntologyTerm.ENTITY_NAME, ontologyTerm);
		}
	}

	/**
	 * Note: package-private for testability
	 * 
	 * @param categories
	 */
	void deleteCategories(Iterable<Category> categories)
	{
		for (Entity category : categories)
		{
			Iterable<CategoricalValue> categoricalValues = dataService.findAll(CategoricalValue.ENTITY_NAME,
					new QueryImpl().eq(CategoricalValue.VALUE, category), CategoricalValue.class);

			for (CategoricalValue categoricalValue : categoricalValues)
			{
				dataService.delete(CategoricalValue.ENTITY_NAME, categoricalValue);
			}
			dataService.delete(Category.ENTITY_NAME, category);
		}
	}

	/**
	 * Count the number of times a protocol of feature is referred to from a(n other) protocol
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            feature of protocol that is referred to
	 * 
	 * @return the number of referring protocols
	 */
	int countReferringEntities(Characteristic characteristic, List<Entity> entitiesList)
	{
		int entityCount = 0;
		Class<? extends Characteristic> clazz = characteristic.getClass();

		for (Entity entity : entitiesList)
		{
			if (entity.equals(characteristic))
			{
				continue;
			}

			if (entity.getClass().equals(Protocol.class))
			{
				Protocol protocol = (Protocol) entity;
				if ((clazz.equals(ObservableFeature.class) && protocol.getFeatures().contains(characteristic) || (clazz
						.equals(Protocol.class) && protocol.getSubprotocols().contains(characteristic))))
				{
					entityCount++;
				}
			}
			if (entity.getClass().equals(DataSet.class))
			{
				DataSet dataSet = (DataSet) entity;
				if ((clazz.equals(Protocol.class) && dataSet.getProtocolUsed().getIdentifier()
						.equals(characteristic.getIdentifier())))
				{
					entityCount++;
				}
			}
			if (entity.getClass().equals(ObservedValue.class))
			{
				ObservedValue value = (ObservedValue) entity;
				if (clazz.equals(ObservableFeature.class) && value.getFeature().equals(characteristic))
				{
					entityCount++;
				}
			}
		}
		return entityCount;
	}
}
