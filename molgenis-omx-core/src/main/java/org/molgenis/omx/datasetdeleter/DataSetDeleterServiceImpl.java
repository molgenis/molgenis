package org.molgenis.omx.datasetdeleter;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional(rollbackFor = MolgenisDataException.class)
	public DataSet delete(String dataSetIdentifier, boolean deleteMetaData)
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier));

		deleteData(dataSet);
		if (deleteMetaData)
		{
			List<Protocol> allProtocols = dataService.findAllAsList(Protocol.ENTITY_NAME, new QueryImpl());
			Protocol protocolUsed = dataSet.getProtocolUsed();
			dataService.delete(DataSet.ENTITY_NAME, dataSet);
			deleteProtocol(protocolUsed, allProtocols);
		}
		searchService.deleteDocumentsByType(dataSet.getIdentifier());

		return dataSet;
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
		int count = 0;
		List<ObservationSet> observationSets = dataService.findAllAsList(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataset.getIdValue()));

		List<ObservedValue> observedValues = new ArrayList<ObservedValue>();
		for (ObservationSet observationSet : observationSets)
		{
			List<ObservedValue> list = dataService.findAllAsList(ObservedValue.ENTITY_NAME,
					new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet));

			observedValues.addAll(list);

			if (count % 20 == 0)
			{
				dataService.delete(ObservedValue.ENTITY_NAME, observedValues);
				observedValues = new ArrayList<ObservedValue>();
			}
			count++;
		}
		if (observedValues.size() != 0)
		{
			dataService.delete(ObservedValue.ENTITY_NAME, observedValues);
		}
		dataService.delete(ObservationSet.ENTITY_NAME, observationSets);
	}

	/**
	 * Deletes all subprotocols which do not have multiple Protocols referencing them
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            protocols that should be deleted
	 */
	List<Protocol> deleteProtocol(Protocol protocol, List<Protocol> allProtocols)
	{
		boolean deleteInBatch = true;
		List<Protocol> subprotocols = protocol.getSubprotocols();
		// check if any of the subprotocols had subprotocols of its own
		for (Protocol subprotocol : subprotocols)
		{
			if (subprotocol.getSubprotocols().size() > 0) deleteInBatch = false;
		}
		for (Protocol subprotocol : subprotocols)
		{
			int superprotocolcount = countReferringProtocols(subprotocol, allProtocols);
			// only delete if there is only one parent protocol
			if (superprotocolcount <= 1)
			{
				if (!deleteInBatch)
				{
					allProtocols = deleteProtocol(subprotocol, allProtocols);
				}
				else
				{
					List<ObservableFeature> features = subprotocol.getFeatures();
					deleteFeatures(features, allProtocols);
				}
			}
		}
		List<ObservableFeature> features = protocol.getFeatures();
		if (deleteInBatch)
		{
			dataService.delete(Protocol.ENTITY_NAME, subprotocols);
			allProtocols.removeAll(subprotocols);
		}
		dataService.delete(Protocol.ENTITY_NAME, protocol);
		deleteFeatures(features, allProtocols);
		allProtocols.remove(protocol);
		return allProtocols;
	}

	/**
	 * Deletes all features which do not have multiple Protocols referencing them
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            features that should be deleted
	 */
	void deleteFeatures(List<ObservableFeature> features, List<Protocol> allProtocols)
	{
		List<ObservableFeature> removableFeatures = new ArrayList<ObservableFeature>();

		for (ObservableFeature feature : features)
		{
			List<Category> categories = dataService.findAllAsList(Category.ENTITY_NAME,
					new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature.getIdValue()));
			deleteCategories(categories);

			int protocolcount = countReferringProtocols(feature, allProtocols);
			if (protocolcount <= 1)
			{
				removableFeatures.add(feature);
			}
		}
		dataService.delete(ObservableFeature.ENTITY_NAME, removableFeatures);
	}

	/**
	 * Note: package-private for testability
	 * 
	 * @param categories
	 * @throws DatabaseException
	 */
	void deleteCategories(List<Category> categories)
	{
		for (Category category : categories)
		{
			List<CategoricalValue> categoricalValues = dataService.findAllAsList(CategoricalValue.ENTITY_NAME,
					new QueryImpl().eq(CategoricalValue.VALUE, category.getIdValue()));

			for (CategoricalValue cat : categoricalValues)
			{
				dataService.delete(CategoricalValue.ENTITY_NAME, cat);
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
	int countReferringProtocols(Characteristic characteristic, List<Protocol> allProtocols)
	{
		int protocolcount = 0;
		Class<? extends Characteristic> clazz = characteristic.getClass();
		for (Protocol p : allProtocols)
		{
			if ((clazz.equals(ObservableFeature.class) && p.getFeatures().contains(characteristic) || (clazz
					.equals(Protocol.class) && p.getSubprotocols().contains(characteristic))))
			{
				protocolcount++;
			}
		}
		return protocolcount;
	}
}
