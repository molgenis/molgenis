package org.molgenis.omx.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.JDBCMetaDatabase;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;

/**
 * DataSetTable
 * 
 * If this table is too slow consider creating database an index on the ObservedValue table : One on the fields
 * Feature-Value and one on ObservationSet-Feature-Value
 * 
 * @deprecated to be replaced by DataApi
 */
@Deprecated
public class DataSetTable extends AbstractFilterableTupleTable
{
	private static final Logger logger = Logger.getLogger(DataSetTable.class);
	private DataSet dataSet;
	private final DataService dataService;
	private List<Field> columns;
	private final JDBCMetaDatabase jdbcMetaDatabase;

	public DataSetTable(DataSet set, DataService dataService, JDBCMetaDatabase jdbcMetaDatabase) throws TableException,
			DatabaseException
	{
		if (set == null) throw new TableException("DataSet cannot be null");
		if (dataService == null) throw new TableException("DataService cannot be null");
		if (jdbcMetaDatabase == null) throw new TableException("JDBCMetaDatabase cannot be null");
		this.dataSet = set;
		this.dataService = dataService;
		this.jdbcMetaDatabase = jdbcMetaDatabase;

		setFirstColumnFixed(true);
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

						field.setEntity(jdbcMetaDatabase.getEntity(ObservableFeature.class.getSimpleName()));

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

				// Id column (is id of ObservationSet)
				Field field = new Field("id");
				field.setDescription("id");
				field.setEntity(jdbcMetaDatabase.getEntity(ObservableFeature.class.getSimpleName()));
				field.setLabel("id");
				field.setType(MolgenisFieldTypes.getType("int"));

				columns.add(field);
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
			QueryImpl query = (QueryImpl) createQuery();

			if (getLimit() > 0)
			{
				query.setPageSize(getLimit());
			}

			if (getOffset() > 0)
			{
				query.setOffset(getOffset());
			}

			return new DataSetTableIterator(dataService, getColumns(), query);
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
			Query query = createQuery();
			return query == null ? 0 : (int) dataService.count(ObservationSet.ENTITY_NAME, query);
		}
		catch (DatabaseException e)
		{
			logger.error("DatabaseException getCount", e);
			throw new TableException(e);
		}

	}

	// Creates the query based on the provided filters
	// Returns null if we already now there wil be no results
	private Query createQuery() throws TableException, DatabaseException
	{

		Query query;

		if (getFilters().isEmpty())
		{
			query = new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet);
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

				queryRules.add(new QueryRule(ObservedValue.FEATURE, Operator.EQUALS, dataService.findOne(
						ObservableFeature.ENTITY_NAME,
						new QueryImpl().eq(ObservableFeature.IDENTIFIER, filter.getField()))));
				queryRules.add(new QueryRule(ObservedValue.VALUE, filter.getOperator(), filter.getValue()));
			}

			List<ObservedValue> observedValues = dataService.findAllAsList(ObservedValue.ENTITY_NAME, new QueryImpl(
					queryRules));

			// No results
			if (observedValues.isEmpty())
			{
				return null;
			}

			List<Integer> observationSetIds = new ArrayList<Integer>();
			for (ObservedValue observedValue : observedValues)
			{
				if (!observationSetIds.contains(observedValue.getObservationSet().getId()))
				{
					observationSetIds.add(observedValue.getObservationSet().getId());
				}
			}

			query = new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet).in(ObservationSet.ID, observationSetIds);
		}

		return query;

	}
}
