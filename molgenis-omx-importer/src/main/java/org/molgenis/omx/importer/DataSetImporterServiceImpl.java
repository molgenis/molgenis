package org.molgenis.omx.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataSetImporterServiceImpl implements DataSetImporterService
{
	private static final Logger LOG = Logger.getLogger(DataSetImporterServiceImpl.class);
	private static final String DATASET_SHEET_PREFIX = "dataset_";
	private static final String DATASET_ROW_IDENTIFIER_HEADER = "DataSet_Row_Id";
	private final DataService dataService;
	private final ValueConverter valueConverter;

	@Autowired
	public DataSetImporterServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
		this.valueConverter = new ValueConverter(dataService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.importer.DataSetImporter#importDataSet(java.io.File, java.util.List)
	 */
	@Override
	@Transactional(rollbackFor =
	{ IOException.class, ValueConverterException.class })
	public void importDataSet(File file, List<String> dataSetEntityNames) throws IOException, ValueConverterException
	{
		EntitySource entitySource = dataService.createEntitySource(file);
		try
		{
			for (String entityName : entitySource.getEntityNames())
			{
				if (dataSetEntityNames.contains(entityName))
				{
					LOG.info("importing dataset " + entityName + " from file " + file + "...");

					Repository<? extends Entity> repo = entitySource.getRepositoryByEntityName(entityName);
					try
					{
						importSheet(repo, entityName);
					}
					finally
					{
						repo.close();
					}
				}

			}
		}
		finally
		{
			entitySource.close();
		}
	}

	@Override
	private void importSheet(Repository<? extends Entity> repo, String sheetName) throws IOException,
			ValueConverterException
	{
		String identifier = sheetName.substring(DATASET_SHEET_PREFIX.length());

		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl().eq(DataSet.IDENTIFIER, identifier));
		if (dataSet == null)
		{
			throw new MolgenisDataException("dataset '" + identifier + "' does not exist in db");
		}

		Iterator<AttributeMetaData> colIt = repo.getAttributes().iterator();
		if (colIt == null || !colIt.hasNext()) throw new IOException("sheet '" + sheetName + "' contains no header");

		// create feature map
		Map<String, ObservableFeature> featureMap = new LinkedHashMap<String, ObservableFeature>();
		while (colIt.hasNext())
		{
			AttributeMetaData attr = colIt.next();
			String featureIdentifier = attr.getName();
			if (featureIdentifier != null && !featureIdentifier.isEmpty())
			{
				if (!featureIdentifier.equalsIgnoreCase(DATASET_ROW_IDENTIFIER_HEADER))
				{
					ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME,
							new QueryImpl().eq(ObservableFeature.IDENTIFIER, featureIdentifier));

					if (feature == null)
					{
						throw new MolgenisDataException(
								ObservableFeature.class.getSimpleName()
										+ " with identifier '"
										+ featureIdentifier
										+ "' does not exist. This is probably due to the fact that the feature is in the dataset_"
										+ identifier + " but is not annotated in the observablefeature entity");

					}
					featureMap.put(featureIdentifier, feature);
				}
			}
			else throw new MolgenisDataException("sheet '" + sheetName + "' contains empty column header");
		}
		if (featureMap.isEmpty()) throw new MolgenisDataException("sheet '" + sheetName + "' contains no header");

		int rownr = 0;
		int transactionRows = 10;// ;Math.max(1, 5000 / featureMap.size());

		for (Entity entity : repo)
		{
			// Skip empty rows
			if (!EntityUtils.isEmpty(entity))
			{
				List<ObservedValue> obsValueList = new ArrayList<ObservedValue>();
				Map<String, List<Value>> valueMap = new HashMap<String, List<Value>>();

				String rowIdentifier = entity.getString(DATASET_ROW_IDENTIFIER_HEADER);
				if (rowIdentifier == null) rowIdentifier = UUID.randomUUID().toString();

				// create observation set
				ObservationSet observationSet = new ObservationSet();
				observationSet.setIdentifier(rowIdentifier);
				observationSet.setPartOfDataSet(dataSet);
				dataService.add(ObservationSet.ENTITY_NAME, observationSet);

				for (Map.Entry<String, ObservableFeature> entry : featureMap.entrySet())
				{

					Value value = valueConverter.fromEntity(entity, entry.getKey(), entry.getValue());
					if (value != null)
					{
						// create observed value
						ObservedValue observedValue = new ObservedValue();
						observedValue.setFeature(entry.getValue());
						observedValue.setValue(value);
						observedValue.setObservationSet(observationSet);

						List<Value> valueList = valueMap.get(value.getEntityName());
						if (valueList == null)
						{
							valueList = new ArrayList<Value>();
							valueMap.put(value.getEntityName(), valueList);
						}
						valueList.add(value);
						obsValueList.add(observedValue);
					}

				}
				for (Map.Entry<String, List<Value>> entry : valueMap.entrySet())
					dataService.add(entry.getKey(), entry.getValue());

				dataService.add(ObservedValue.ENTITY_NAME, obsValueList);
			}

			if (++rownr % transactionRows == 0)
			{
				CrudRepository<? extends Entity> dataSetRepo = dataService
						.getCrudRepository(ObservationSet.ENTITY_NAME);
				dataSetRepo.flush();
				dataSetRepo.clearCache();
			}
		}
		if (rownr % transactionRows != 0)
		{
			CrudRepository<? extends Entity> dataSetRepo = dataService.getCrudRepository(ObservationSet.ENTITY_NAME);
			dataSetRepo.flush();
			dataSetRepo.clearCache();
		}
	}
}
