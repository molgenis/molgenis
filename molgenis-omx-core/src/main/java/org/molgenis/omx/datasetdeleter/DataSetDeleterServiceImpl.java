package org.molgenis.omx.datasetdeleter;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
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
	private final Database database;
	private final SearchService searchService;

	@Autowired
	public DataSetDeleterServiceImpl(Database database, SearchService searchService)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		if (searchService == null) throw new IllegalArgumentException("Search service is null");
		this.database = database;
		this.searchService = searchService;
	}

	@Override
	@Transactional(rollbackFor = DatabaseException.class)
	public DataSet delete(String dataSetIdentifier, boolean deleteMetaData) throws DatabaseException
	{
		DataSet dataSet = DataSet.findByIdentifier(database, dataSetIdentifier);

		deleteData(dataSet);
		if (deleteMetaData)
		{
			List<Protocol> allProtocols = database.find(Protocol.class);
			Protocol protocolUsed = dataSet.getProtocolUsed();
			database.remove(dataSet);
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
	void deleteData(DataSet dataset) throws DatabaseException
	{
		int count = 0;
		List<ObservationSet> observationSets = database.find(ObservationSet.class, new QueryRule(
				ObservationSet.PARTOFDATASET, Operator.EQUALS, dataset.getIdValue()));
		List<ObservedValue> observedValues = new ArrayList<ObservedValue>();
		for (ObservationSet observationSet : observationSets)
		{
			observedValues.addAll(database.find(ObservedValue.class, new QueryRule(
					ObservedValue.OBSERVATIONSET_IDENTIFIER, Operator.EQUALS, observationSet.getIdentifier())));
			if (count % 20 == 0)
			{
				database.remove(observedValues);
				observedValues = new ArrayList<ObservedValue>();
			}
			count++;
		}
		if (observedValues.size() != 0)
		{
			database.remove(observedValues);
		}
		database.remove(observationSets);
	}

	/**
	 * Deletes all subprotocols which do not have multiple Protocols referencing them
	 * 
	 * Note: package-private for testability
	 * 
	 * @param the
	 *            protocols that should be deleted
	 */
	List<Protocol> deleteProtocol(Protocol protocol, List<Protocol> allProtocols) throws DatabaseException
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
			database.remove(subprotocols);
			allProtocols.removeAll(subprotocols);
		}
		database.remove(protocol);
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
	void deleteFeatures(List<ObservableFeature> features, List<Protocol> allProtocols) throws DatabaseException
	{
		List<ObservableFeature> removableFeatures = new ArrayList<ObservableFeature>();

		for (ObservableFeature feature : features)
		{
			List<Category> categories = database.find(Category.class, new QueryRule(Category.OBSERVABLEFEATURE,
					Operator.EQUALS, feature.getIdValue()));
			deleteCategories(categories);
			int protocolcount = countReferringProtocols(feature, allProtocols);
			if (protocolcount <= 1)
			{
				removableFeatures.add(feature);
			}
		}
		database.remove(removableFeatures);
	}

	/**
	 * Note: package-private for testability
	 * 
	 * @param categories
	 * @throws DatabaseException
	 */
	void deleteCategories(List<Category> categories) throws DatabaseException
	{
		for (Category category : categories)
		{
			List<CategoricalValue> categoricalValues = database.find(CategoricalValue.class, new QueryRule(
					CategoricalValue.VALUE, Operator.EQUALS, category.getIdValue()));
			for (CategoricalValue cat : categoricalValues)
			{
				database.remove(cat);
			}
			database.remove(category);
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
			if ((clazz.equals(ObservableFeature.class) && p.getFeatures_Id().contains(characteristic.getId()) || (clazz
					.equals(Protocol.class) && p.getSubprotocols_Id().contains(characteristic.getId()))))
			{
				System.out.println(p.getIdentifier());
				protocolcount++;
			}
		}
		return protocolcount;
	}
}
