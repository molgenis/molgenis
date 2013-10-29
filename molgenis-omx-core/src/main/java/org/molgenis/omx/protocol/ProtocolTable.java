package org.molgenis.omx.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.utils.I18nTools;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

public class ProtocolTable extends AbstractFilterableTupleTable implements DatabaseTupleTable
{
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_ID = "id";
	private static final String FIELD_IDENTIFIER = "identifier";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_DESCRIPTION = "description";
	private static final String FIELD_PATH = "path";
	private static final String DATA_TYPE = "dataType";
	private static final String FIELD_CATEGORY = "category";

	private final Protocol protocol;
	private Database db;

	public ProtocolTable(Protocol protocol, Database db) throws TableException
	{
		if (protocol == null) throw new TableException("protocol cannot be null");
		this.protocol = protocol;
		if (db == null) throw new TableException("db cannot be null");
		this.db = db;
		setFirstColumnFixed(false);
	}

	@Override
	public List<Field> getAllColumns() throws TableException
	{
		List<Field> columns = new ArrayList<Field>();
		columns.add(new Field(FIELD_TYPE));
		columns.add(new Field(FIELD_ID));
		columns.add(new Field(FIELD_IDENTIFIER));
		columns.add(new Field(FIELD_NAME));
		columns.add(new Field(FIELD_DESCRIPTION));
		columns.add(new Field(FIELD_PATH));
		columns.add(new Field(FIELD_CATEGORY));
		return columns;
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();
		try
		{
			createTuplesRec("", protocol, tuples);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		return tuples.iterator();
	}

	private void createTuplesRec(String protocolPath, Protocol protocol, List<Tuple> tuples) throws DatabaseException
	{
		List<Protocol> subProtocols = protocol.getSubprotocols();
		if (!subProtocols.isEmpty())
		{
			for (Protocol p : subProtocols)
			{
				StringBuilder pathBuilder = new StringBuilder();
				if (!protocolPath.isEmpty()) pathBuilder.append(protocolPath).append('.');
				String name = p.getName();
				final String path = pathBuilder.append(p.getId()).toString();
				String description = p.getDescription() == null ? StringUtils.EMPTY : I18nTools.get(p.getDescription())
						.replaceAll("[^a-zA-Z0-9 ]", " ");
				KeyValueTuple tuple = new KeyValueTuple();
				tuple.set(FIELD_TYPE, Protocol.class.getSimpleName().toLowerCase());
				tuple.set(FIELD_ID, p.getId());
				tuple.set(FIELD_IDENTIFIER, p.getIdentifier());
				tuple.set(FIELD_NAME, name);
				tuple.set(FIELD_DESCRIPTION, description);
				tuple.set(FIELD_PATH, path);
				tuples.add(tuple);

				// recurse
				createTuplesRec(pathBuilder.toString(), p, tuples);
			}
		}
		else
		{
			for (ObservableFeature feature : protocol.getFeatures())
			{
				StringBuilder pathBuilder = new StringBuilder();
				String name = feature.getName();
				String description = feature.getDescription() == null ? StringUtils.EMPTY : I18nTools.get(
						feature.getDescription()).replaceAll("[^a-zA-Z0-9 ]", " ");
				String path = pathBuilder.append(protocolPath).append(".F").append(feature.getId()).toString();
				StringBuilder categoryValue = new StringBuilder();

				for (Category c : Category.find(db, new QueryRule(Category.OBSERVABLEFEATURE_IDENTIFIER,
						Operator.EQUALS, feature.getIdentifier())))
				{
					String categoryName = c.getName() == null ? StringUtils.EMPTY : c.getName().replaceAll(
							"[^a-zA-Z0-9 ]", " ");
					categoryValue.append(categoryName).append(' ');
				}

				KeyValueTuple tuple = new KeyValueTuple();
				tuple.set(FIELD_TYPE, ObservableFeature.class.getSimpleName().toLowerCase());
				tuple.set(FIELD_ID, feature.getId());
				tuple.set(FIELD_IDENTIFIER, feature.getIdentifier());
				tuple.set(FIELD_NAME, name);
				tuple.set(FIELD_DESCRIPTION, description);
				tuple.set(FIELD_PATH, path);
				tuple.set(DATA_TYPE, feature.getDataType());
				tuple.set(FIELD_CATEGORY, categoryValue.toString());
				tuples.add(tuple);
			}

		}
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

	/**
	 * Count the number of protocols and features of this protocol (excluding
	 * this protocol itself)
	 */
	@Override
	public int getCount() throws TableException
	{
		AtomicInteger count = new AtomicInteger(0);
		try
		{
			countTuplesRec(protocol, count);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		return count.get();
	}

	private void countTuplesRec(Protocol protocol, AtomicInteger count) throws DatabaseException
	{
		List<Protocol> subProtocols = protocol.getSubprotocols();
		if (!subProtocols.isEmpty())
		{
			for (Protocol p : subProtocols)
			{
				count.incrementAndGet();
				countTuplesRec(p, count);
			}
		}
		else
		{
			List<ObservableFeature> features = protocol.getFeatures();
			if (!features.isEmpty()) count.addAndGet(features.size());
		}
	}
}
