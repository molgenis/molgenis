package org.molgenis.omx.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.util.tuple.Tuple;

/**
 * DataSetTable
 * 
 * If this table is too slow consider creating database an index on the ObservedValue table : One on the fields
 * Feature-Value and one on ObservationSet-Feature-Value
 * 
 */
public class DataSetTable extends AbstractFilterableTupleTable implements DatabaseTupleTable
{
	private static final Logger logger = Logger.getLogger(DataSetTable.class);
	private DataSet dataSet;
	private Database db;
	private List<Field> columns;

	public DataSetTable(DataSet set, Database db) throws TableException
	{
		if (set == null) throw new TableException("DataSet cannot be null");
		this.dataSet = set;
		if (db == null) throw new TableException("db cannot be null");
		setDb(db);
		setFirstColumnFixed(true);
	}

	@Override
	public Database getDb()
	{
		return db;
	}

	@Override
	public void setDb(Database db)
	{
		this.db = db;
	}

	public DataSet getDataSet()
	{
		return dataSet;
	}

	public void setDataSet(DataSet dataSet)
	{
		this.dataSet = dataSet;
	}

	@Override
	public List<Field> getAllColumns() throws TableException
	{
		if (columns == null) initColumnsFromDb();
		return Collections.unmodifiableList(columns);
	}

	private void initColumnsFromDb() throws TableException
	{
		try
		{
			Protocol protocol = dataSet.getProtocolUsed();

			// if dataset has protocol-used, determine columns from protocol
			if (protocol != null)
			{
				List<ObservableFeature> features = new ArrayList<ObservableFeature>();
				getFeatures(protocol, features);

				if (features != null && !features.isEmpty())
				{
					columns = new ArrayList<Field>(features.size());
					for (ObservableFeature feature : features)
					{
						FieldType fieldType = MolgenisFieldTypes.getType(feature.getDataType());

						Field field = new Field(feature.getIdentifier());
						field.setEntity(db.getMetaData().getEntity(ObservableFeature.class.getSimpleName()));
						field.setLabel(feature.getName());
						field.setType(fieldType);
						field.setName(feature.getIdentifier());

						FieldTypeEnum enumType = fieldType.getEnumType();
						if (enumType.equals(FieldTypeEnum.XREF) || enumType.equals(FieldTypeEnum.MREF))
						{
							field.setXRefEntity(Characteristic.class.getSimpleName());
							field.setXrefField(Characteristic.NAME);
						}
						else if (enumType.equals(FieldTypeEnum.CATEGORICAL))
						{
							field.setXRefEntity(Category.class.getSimpleName());
							field.setXrefField(org.molgenis.omx.observ.Category.NAME);
						}

						columns.add(field);
					}
				}
				else
				{
					columns = Collections.emptyList();
				}
			}
		}
		catch (Exception e)
		{
			throw new TableException(e);
		}
	}

	private void getFeatures(Protocol protocol, List<ObservableFeature> features) throws DatabaseException
	{
		// store features
		features.addAll(protocol.getFeatures());

		for (Protocol subProtocol : protocol.getSubprotocols())
		{
			getFeatures(subProtocol, features);
		}
	}

	@Override
	public DataSetTableIterator iterator()
	{

		try
		{
			Query<ObservationSet> query = createQuery();
			if (getLimit() > 0)
			{
				query.limit(getLimit());
			}

			if (getOffset() > 0)
			{
				query.offset(getOffset());
			}

			return new DataSetTableIterator(db, getColumns(), query);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	public int getCount() throws TableException
	{
		try
		{
			Query<ObservationSet> query = createQuery();
			return query == null ? 0 : query.count();
		}
		catch (DatabaseException e)
		{
			logger.error("DatabaseException getCount", e);
			throw new TableException(e);
		}

	}

	public void add(TupleTable table) throws TableException
	{
		try
		{
			getDb().beginTx();

			// validate features
			Map<String, ObservableFeature> featureMap = new TreeMap<String, ObservableFeature>();
			for (Field f : table.getAllColumns())
			{
				try
				{
					ObservableFeature feature = ObservableFeature.findByIdentifier(getDb(), f.getName());
					if (feature == null)
					{
						throw new TableException("add failed: " + f.getName() + " not known ObservableFeature");
					}
					else
					{
						featureMap.put(f.getName(), feature);
					}
				}
				catch (DatabaseException e)
				{
					throw new TableException(e);
				}
			}

			// load values
			for (Tuple t : table)
			{
				ObservationSet es = new ObservationSet();
				es.setPartOfDataSet(dataSet.getId());
				getDb().add(es);

				List<ObservedValue> values = new ArrayList<ObservedValue>();
				for (String name : t.getColNames())
				{
					ObservableFeature feature = featureMap.get(name);

					ObservedValue v = new ObservedValue();
					v.setObservationSet(es.getId());
					v.setFeature(feature);
					v.setValue(ValueConverter.fromTuple(t, name, db, feature));
					values.add(v);
				}
				getDb().add(values);
			}

			getDb().commitTx();

		}
		catch (Exception e)
		{
			try
			{
				getDb().rollbackTx();
			}
			catch (DatabaseException e1)
			{
			}
			throw new TableException(e);
		}
	}

	// Creates the query based on the provided filters
	// Returns null if we already now there wil be no results
	private Query<ObservationSet> createQuery() throws TableException, DatabaseException
	{

		Query<ObservationSet> query;

		if (getFilters().isEmpty())
		{
			query = getDb().query(ObservationSet.class).eq(ObservationSet.PARTOFDATASET, dataSet.getId());
		}
		else
		{
			// For now only single simple queries are supported
			List<QueryRule> queryRules = new ArrayList<QueryRule>();

			for (QueryRule filter : getFilters())
			{
				if ((filter.getOperator() != Operator.EQUALS) && (filter.getOperator() != Operator.LIKE))
				{
					// value is always a String so LESS etc. can't be
					// supported, NOT queries are not supported yet
					throw new UnsupportedOperationException("Operator [" + filter.getOperator()
							+ "] not yet implemented, only EQUALS and LIKE are supported.");

				}

				// Null values come to us as String 'null'
				if ((filter.getValue() != null) && (filter.getValue() instanceof String)
						&& ((String) filter.getValue()).equalsIgnoreCase("null"))
				{
					filter.setValue(null);
				}

				queryRules.add(new QueryRule(ObservedValue.FEATURE_IDENTIFIER, Operator.EQUALS, filter.getField()));
				queryRules.add(new QueryRule(ObservedValue.VALUE, filter.getOperator(), filter.getValue()));
			}

			List<ObservedValue> observedValues = getDb().find(ObservedValue.class,
					queryRules.toArray(new QueryRule[queryRules.size()]));

			// No results
			if (observedValues.isEmpty())
			{
				return null;
			}

			List<Integer> observationSetIds = new ArrayList<Integer>();
			for (ObservedValue observedValue : observedValues)
			{
				if (!observationSetIds.contains(observedValue.getObservationSet_Id()))
				{
					observationSetIds.add(observedValue.getObservationSet_Id());
				}
			}

			query = getDb().query(ObservationSet.class).eq(ObservationSet.PARTOFDATASET, dataSet.getId())
					.in(ObservationSet.ID, observationSetIds);
		}

		return query;

	}
}
