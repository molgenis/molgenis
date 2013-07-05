package org.molgenis.elasticsearch.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.base.Joiner;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.model.elements.Field;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

/**
 * Creates an IndexRequest for indexing entities with ElasticSearch
 * 
 * @author erwin
 * 
 */
public class IndexRequestGenerator
{
	private static final Logger LOG = Logger.getLogger(IndexRequestGenerator.class);
	private final Client client;
	private final String indexName;

	public IndexRequestGenerator(Client client, String indexName)
	{
		if (client == null)
		{
			throw new IllegalArgumentException("Client is null");
		}

		if (indexName == null)
		{
			throw new IllegalArgumentException("IndexName is null");
		}

		this.client = client;
		this.indexName = indexName;
	}

	public BulkRequestBuilder buildIndexRequest(String documentName, Iterable<? extends Entity> entities)
	{
		BulkRequestBuilder bulkRequest = client.prepareBulk();

		int count = 0;
		for (Entity entity : entities)
		{
			Object id = entity.getIdValue();
			Map<String, Object> doc = new HashMap<String, Object>();
			for (String field : entity.getFields())
			{
				doc.put(field, entity.get(field));

			}

			IndexRequestBuilder request;
			if (id == null)
			{
				request = client.prepareIndex(indexName, documentName);
			}
			else
			{
				request = client.prepareIndex(indexName, documentName, id + "");
			}

			request.setSource(doc);
			bulkRequest.add(request);
			LOG.info("Added [" + (++count) + "] documents");
		}

		return bulkRequest;
	}

	public Iterable<BulkRequestBuilder> buildIndexRequest(final String documentName, final TupleTable tupleTable)
	{
		return new Iterable<BulkRequestBuilder>()
		{
			@Override
			public Iterator<BulkRequestBuilder> iterator()
			{
				try
				{
					return indexRequestIterator(documentName, tupleTable);
				}
				catch (TableException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
	}

	private Iterator<BulkRequestBuilder> indexRequestIterator(final String documentName, final TupleTable tupleTable)
			throws TableException
	{
		final Set<String> xrefColumns = new HashSet<String>();
		for (Field field : tupleTable.getColumns())
		{
			boolean isXref = field.getType().getEnumType().equals(FieldTypeEnum.XREF);
			if (isXref) xrefColumns.add(field.getName());
		}

		return new Iterator<BulkRequestBuilder>()
		{
			private final int rows = tupleTable.getCount();
			private final int docsPerBulk = 1000;
			private final Iterator<Tuple> it = tupleTable.iterator();

			private int row = 0;

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public BulkRequestBuilder next()
			{
				BulkRequestBuilder bulkRequest = client.prepareBulk();

				final int maxRow = Math.min(row + docsPerBulk, rows);
				for (; row < maxRow; ++row)
				{
					Tuple tuple = it.next();
					Map<String, Object> doc = new HashMap<String, Object>();
					for (String columnName : tuple.getColNames())
					{
						// Serialize collections to be able to sort on them, elasticsearch does not support sorting on
						// list
						// fields
						Object value = tuple.get(columnName);
						if (value instanceof Collection)
						{
							value = Joiner.on(" , ").join((Collection<?>) value);
						}

						doc.put(columnName, value);
					}

					List<Object> xrefValues = new ArrayList<Object>();
					for (String columnName : tuple.getColNames())
					{
						if (xrefColumns.contains(columnName)) xrefValues.add(tuple.get(columnName));

					}
					doc.put("_xrefvalue", xrefValues);

					IndexRequestBuilder request = client.prepareIndex(indexName, documentName);

					request.setSource(doc);
					bulkRequest.add(request);
					if ((row + 1) % 100 == 0) LOG.info("Added [" + (row + 1) + "] documents");
				}
				LOG.info("Added [" + row + "] documents");

				return bulkRequest;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
