package org.molgenis.omx.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
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
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * DataSetTable
 * 
 * If this table is too slow consider creating database an index on the
 * ObservedValue table : One on the fields Feature-Value and one on
 * ObservationSet-Feature-Value
 * 
 */
public class DataSetTable extends AbstractFilterableTupleTable implements DatabaseTupleTable
{
	private static Logger logger = Logger.getLogger(DataSetTable.class);
	private DataSet dataSet;
	private Database db;
	private final ApplicationContext ctx;
	private List<Field> columns;

	public DataSetTable(DataSet set, Database db, ApplicationContext ctx) throws TableException
	{
		if (set == null) throw new TableException("DataSet cannot be null");
		this.dataSet = set;
		if (db == null) throw new TableException("db cannot be null");
		setDb(db);
		if (ctx == null) throw new TableException("ctx cannot be null");
		this.ctx = ctx;
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
			Integer protocolId = dataSet.getProtocolUsed_Id();
			List<Protocol> protocols = db.find(Protocol.class, new QueryRule(Protocol.ID, Operator.EQUALS, protocolId));

			// if dataset has protocol-used, determine columns from protocol
			if (protocols != null && !protocols.isEmpty())
			{
				List<Integer> featureIds = getFeatureIds(protocols);
				List<ObservableFeature> features = db.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
						Operator.IN, featureIds));
				if (features != null && !features.isEmpty())
				{
					columns = new ArrayList<Field>(features.size());

					for (ObservableFeature feature : features)
					{
						Field field = new Field(feature.getIdentifier());
						field.setLabel(feature.getName());
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

	private List<Integer> getFeatureIds(List<Protocol> protocols) throws DatabaseException
	{
		List<Integer> protocolIds = new ArrayList<Integer>();
		for (Protocol protocol : protocols)
			getFeatureIdsRec(protocol, protocolIds);
		return protocolIds;
	}

	private void getFeatureIdsRec(Protocol protocol, List<Integer> featureIds) throws DatabaseException
	{
		// store feature ids
		featureIds.addAll(protocol.getFeatures_Id());

		// recurse sub protocols
		List<Integer> subProtocolIds = protocol.getSubprotocols_Id();
		if (subProtocolIds != null && !subProtocolIds.isEmpty())
		{
			List<Protocol> subProtocols = db.find(Protocol.class, new QueryRule(Protocol.ID, Operator.IN,
					subProtocolIds));
			if (subProtocols != null)
			{
				for (Protocol subProtocol : subProtocols)
					getFeatureIdsRec(subProtocol, featureIds);
			}
		}
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		try
		{
			return getRows().iterator();
		}
		catch (TableException e)
		{
			logger.error("Exception getting iterator", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Tuple> getRows() throws TableException
	{
		try
		{
			List<Tuple> result = new ArrayList<Tuple>();

			Query<ObservationSet> query = createQuery();

			if (query == null)
			{
				return new ArrayList<Tuple>();
			}

			for (ObservationSet os : query.find())
			{

				WritableTuple tuple = new KeyValueTuple();
				tuple.set(ObservedValue.OBSERVATIONSET_ID, os.getIdValue());

				Query<ObservedValue> queryObservedValue = getDb().query(ObservedValue.class);

				List<Field> columns = getColumns();

				// Only retrieve the visible columns
				Collection<String> fieldNames = Collections2.transform(columns, new Function<Field, String>()
				{
					@Override
					public String apply(final Field field)
					{
						return field.getName();
					}
				});

				for (ObservedValue v : queryObservedValue.eq(ObservedValue.OBSERVATIONSET, os.getId())
						.in(ObservedValue.FEATURE_IDENTIFIER, new ArrayList<String>(fieldNames)).find())
				{
					tuple.set(v.getFeature_Identifier(), v.getValue());
				}

				result.add(tuple);
			}

			return result;

		}
		catch (Exception e)
		{
			logger.error("Exception getRows", e);
			throw new TableException(e);
		}

	}

	@Override
	public int getCount() throws TableException
	{
		try
		{
			System.out.println(getFilters());
			return (int) getSearchService().count(dataSet.getName(), getFilters());
		}
		catch (Exception e)
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
			Map<String, Integer> featureMap = new TreeMap<String, Integer>();
			for (Field f : table.getAllColumns())
			{
				try
				{
					List<ObservableFeature> feature = getDb().query(ObservableFeature.class)
							.eq(ObservableFeature.IDENTIFIER, f.getName()).find();
					if (feature.size() != 1)
					{
						throw new TableException("add failed: " + f.getName() + " not known ObservableFeature");
					}
					else
					{
						featureMap.put(f.getName(), feature.get(0).getId());
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
					ObservedValue v = new ObservedValue();
					v.setObservationSet(es.getId());
					v.setFeature(featureMap.get(name));
					v.setValue(t.getString(name));
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
				;
			}
			throw new TableException(e);
		}
	}

	// Creates the query based on the provided filters
	// Returns null if we already now there wil be no results
	private Query<ObservationSet> createQuery() throws TableException, DatabaseException
	{

		List<QueryRule> filters = new ArrayList<QueryRule>(getFilters());

		// Limit the nr of rows
		if (getLimit() > 0)
		{
			filters.add(new QueryRule(Operator.LIMIT, getLimit()));
		}

		if (getOffset() > 0)
		{
			filters.add(new QueryRule(Operator.OFFSET, getOffset()));
		}

		System.out.println(filters);
		SearchRequest request = new SearchRequest(dataSet.getName(), filters,
				Arrays.asList(ObservedValue.OBSERVATIONSET_ID));
		SearchResult searchResult = getSearchService().search(request);

		if (searchResult.getTotalHitCount() == 0)
		{
			return null;
		}

		List<Integer> observationSetIds = new ArrayList<Integer>();
		for (Hit hit : searchResult)
		{
			Integer observationSetId = (Integer) hit.getColumnValueMap().get(ObservedValue.OBSERVATIONSET_ID);
			observationSetIds.add(observationSetId);
		}

		Query<ObservationSet> query = getDb().query(ObservationSet.class)
				.eq(ObservationSet.PARTOFDATASET, dataSet.getId()).in(ObservationSet.ID, observationSetIds);

		return query;

	}

	private SearchService getSearchService()
	{
		return ctx.getBean(SearchService.class);
	}
}
