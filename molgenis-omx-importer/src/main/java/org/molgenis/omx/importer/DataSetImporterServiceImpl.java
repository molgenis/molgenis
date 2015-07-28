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
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.io.TableReader;
import org.molgenis.io.TableReaderFactory;
import org.molgenis.io.TupleReader;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataSetImporterServiceImpl implements DataSetImporterService
{
	private static final Logger LOG = Logger.getLogger(DataSetImporterServiceImpl.class);
	private static final String DATASET_SHEET_PREFIX = "dataset_";
	private static final String DATASET_ROW_IDENTIFIER_HEADER = "DataSet_Row_Id";
	private final Database database;
	private final ValueConverter valueConverter;

	@Autowired
	public DataSetImporterServiceImpl(Database database)
	{
		if (database == null) throw new IllegalArgumentException();
		this.database = database;
		this.valueConverter = new ValueConverter(database);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.importer.DataSetImporter#importDataSet(java.io.File, java.util.List)
	 */
	@Override
	@Transactional(rollbackFor =
	{ IOException.class, DatabaseException.class })
	public void importDataSet(File file, List<String> dataSetEntityNames) throws IOException, DatabaseException
	{
		TableReader tableReader = TableReaderFactory.create(file);
		try
		{
			for (String tableName : tableReader.getTableNames())
			{
				if (dataSetEntityNames.contains(tableName))
				{
					LOG.info("importing dataset " + tableName + " from file " + file + "...");
					TupleReader tupleReader = tableReader.getTupleReader(tableName);
					try
					{
						importSheet(tupleReader, tableName);
					}
					finally
					{
						tupleReader.close();
					}
				}
			}
		}
		finally
		{
			tableReader.close();
		}
	}

	private void importSheet(TupleReader sheetReader, String sheetName) throws DatabaseException, IOException
	{
		String identifier = sheetName.substring(DATASET_SHEET_PREFIX.length());

		DataSet dataSet = DataSet.findByIdentifier(database, identifier);
		if (dataSet == null)
		{
			throw new DatabaseException("dataset '" + identifier + "' does not exist in db");
		}

		Iterator<String> colIt = sheetReader.colNamesIterator();
		if (colIt == null || !colIt.hasNext()) throw new IOException("sheet '" + sheetName + "' contains no header");

		// create feature map
		Map<String, ObservableFeature> featureMap = new LinkedHashMap<String, ObservableFeature>();
		while (colIt.hasNext())
		{
			String featureIdentifier = colIt.next();
			if (featureIdentifier != null && !featureIdentifier.isEmpty())
			{
				if (!featureIdentifier.equalsIgnoreCase(DATASET_ROW_IDENTIFIER_HEADER))
				{
					ObservableFeature feature = ObservableFeature.findByIdentifier(database, featureIdentifier);
					if (feature == null)
					{
						throw new DatabaseException(
								ObservableFeature.class.getSimpleName()
										+ " with identifier '"
										+ featureIdentifier
										+ "' does not exist. This is probably due to the fact that the feature is in the dataset_"
										+ identifier + " but is not annotated in the observablefeature entity");

					}
					featureMap.put(featureIdentifier, feature);
				}
			}
			else throw new DatabaseException("sheet '" + sheetName + "' contains empty column header");
		}
		if (featureMap.isEmpty()) throw new DatabaseException("sheet '" + sheetName + "' contains no header");

		int rownr = 0;
		int transactionRows = Math.max(1, 5000 / featureMap.size());

		for (Tuple row : sheetReader)
		{
			// Skip empty rows
			if (!row.isEmpty())
			{
				List<ObservedValue> obsValueList = new ArrayList<ObservedValue>();
				Map<Class<? extends Value>, List<Value>> valueMap = new HashMap<Class<? extends Value>, List<Value>>();

				String rowIdentifier = row.getString(DATASET_ROW_IDENTIFIER_HEADER);
				if (rowIdentifier == null) rowIdentifier = UUID.randomUUID().toString();

				// create observation set
				ObservationSet observationSet = new ObservationSet();
				observationSet.setIdentifier(rowIdentifier);
				observationSet.setPartOfDataSet(dataSet);
				database.add(observationSet);

				for (Map.Entry<String, ObservableFeature> entry : featureMap.entrySet())
				{
					Value value;
					try
					{
						value = valueConverter.fromTuple(row, entry.getKey(), entry.getValue());
						if (value != null)
						{
							// create observed value
							ObservedValue observedValue = new ObservedValue();
							observedValue.setFeature(entry.getValue());
							observedValue.setValue(value);
							observedValue.setObservationSet(observationSet);

							List<Value> valueList = valueMap.get(value.getClass());
							if (valueList == null)
							{
								valueList = new ArrayList<Value>();
								valueMap.put(value.getClass(), valueList);
							}
							valueList.add(value);
							obsValueList.add(observedValue);
						}
					}
					catch (ValueConverterException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				database.add(obsValueList);
				for (Map.Entry<Class<? extends Value>, List<Value>> entry : valueMap.entrySet())
					database.add(entry.getValue());
			}

			if (++rownr % transactionRows == 0)
			{
				database.getEntityManager().flush();
				database.getEntityManager().clear();
			}
		}
		if (rownr % transactionRows != 0)
		{
			database.getEntityManager().flush();
			database.getEntityManager().clear();
		}
	}
}
