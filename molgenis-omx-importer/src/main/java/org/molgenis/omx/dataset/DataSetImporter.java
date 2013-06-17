package org.molgenis.omx.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.molgenis.util.tuple.Tuple;

public class DataSetImporter
{
	private static final Logger LOG = Logger.getLogger(DataSetImporter.class);
	private static final String DATASET_SHEET_PREFIX = "dataset_";
	private final Database db;

	public DataSetImporter(Database db)
	{
		if (db == null) throw new IllegalArgumentException();
		this.db = db;
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

		List<DataSet> dataSets = db.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, identifier));
		if (dataSets == null || dataSets.isEmpty())
		{
			LOG.warn("dataset " + identifier + " does not exist in db");
			return;
		}
		else if (dataSets.size() > 1)
		{
			LOG.warn("multiple datasets exist for identifier " + identifier);
			return;
		}

		DataSet dataSet = dataSets.get(0);

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

				List<ObservedValue> obsValueList = new ArrayList<ObservedValue>();

				// create observation set
				ObservationSet observationSet = new ObservationSet();
				observationSet.setPartOfDataSet(dataSet);
				db.add(observationSet);

				for (Map.Entry<String, ObservableFeature> entry : featureMap.entrySet())
				{
					Value value = ValueConverter.fromTuple(row, entry.getKey(), db, entry.getValue());

					// create observed value
					ObservedValue observedValue = new ObservedValue();
					observedValue.setFeature(entry.getValue());
					observedValue.setValue(value);
					observedValue.setObservationSet(observationSet);

					// add to db
					db.add(value);
					obsValueList.add(observedValue);
				}
				db.add(obsValueList);

				if (++rownr % transactionRows == 0) db.commitTx();
			}
			if (rownr % transactionRows != 0) db.commitTx();
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
