package org.molgenis.omx.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.io.TableReader;
import org.molgenis.io.TableReaderFactory;
import org.molgenis.io.TupleReader;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.DataSetImportedEvent;
import org.molgenis.util.tuple.Tuple;

public class DataSetImporter
{
	private static final Logger LOG = Logger.getLogger(DataSetImporter.class);
	private static final String DATASET_SHEET_PREFIX = "dataset_";
	private final Database db;
	private final ValueConverter valueConverter;

	public DataSetImporter(Database db)
	{
		if (db == null) throw new IllegalArgumentException();
		this.db = db;
		this.valueConverter = new ValueConverter(db);
	}

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

		DataSet dataSet = DataSet.findByIdentifier(db, identifier);
		if (dataSet == null)
		{
			throw new DatabaseException("dataset '" + identifier + "' does not exist in db");
		}

		Iterator<String> colIt = sheetReader.colNamesIterator();
		if (!colIt.hasNext()) throw new IOException("sheet '" + sheetName + "' contains no columns");

		// create observation feature map
		Map<String, ObservableFeature> featureMap = new LinkedHashMap<String, ObservableFeature>();
		while (colIt.hasNext())
		{
			String observableFeatureIdentifier = colIt.next();
			ObservableFeature observableFeature = findObservableFeature(observableFeatureIdentifier);
			featureMap.put(observableFeatureIdentifier, observableFeature);
		}

		int rownr = 0;
		int transactionRows = Math.max(1, 5000 / featureMap.size());
		try
		{
			for (Tuple row : sheetReader)
			{
				if (rownr % transactionRows == 0) db.beginTx();

				// Skip empty rows
				if (!row.isEmpty())
				{
					List<ObservedValue> obsValueList = new ArrayList<ObservedValue>();
					Map<Class<? extends Value>, List<Value>> valueMap = new HashMap<Class<? extends Value>, List<Value>>();

					// create observation set
					ObservationSet observationSet = new ObservationSet();
					observationSet.setPartOfDataSet(dataSet);
					db.add(observationSet);

					for (Map.Entry<String, ObservableFeature> entry : featureMap.entrySet())
					{
						Value value = valueConverter.fromTuple(row, entry.getKey(), entry.getValue());

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
					db.add(obsValueList);
					for (Map.Entry<Class<? extends Value>, List<Value>> entry : valueMap.entrySet())
						db.add(entry.getValue());
				}

				if (++rownr % transactionRows == 0) db.commitTx();
			}
			if (rownr % transactionRows != 0) db.commitTx();

			ApplicationContextProvider.getApplicationContext().publishEvent(
					new DataSetImportedEvent(this, dataSet.getId()));
		}
		catch (DatabaseException e)
		{
			db.rollbackTx();
			throw e;
		}
		catch (Exception e)
		{
			db.rollbackTx();
			throw new IOException(e);
		}
	}

	private ObservableFeature findObservableFeature(String observableFeatureIdentifier) throws DatabaseException,
			IOException
	{
		List<ObservableFeature> observableFeatures = db.find(ObservableFeature.class, new QueryRule(
				ObservableFeature.IDENTIFIER, Operator.EQUALS, observableFeatureIdentifier));
		if (observableFeatures == null || observableFeatures.isEmpty()) throw new IOException("ObservableFeature "
				+ observableFeatureIdentifier + " does not exist in db");
		ObservableFeature observableFeature = observableFeatures.get(0);
		return observableFeature;
	}
}
