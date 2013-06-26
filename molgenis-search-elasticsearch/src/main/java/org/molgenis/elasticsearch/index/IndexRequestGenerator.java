package org.molgenis.elasticsearch.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

	public BulkRequestBuilder buildIndexRequest(String documentName, TupleTable tupleTable)
	{
		BulkRequestBuilder bulkRequest = client.prepareBulk();

		Set<String> xrefColumns = new HashSet<String>();
		try
		{
			for (Field field : tupleTable.getColumns())
			{
				boolean isXref = field.getType().getEnumType().equals(FieldTypeEnum.XREF);
				if (isXref) xrefColumns.add(field.getName());
			}
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}

		int count = 0;
		for (Tuple tuple : tupleTable)
		{
			Map<String, Object> doc = new HashMap<String, Object>();
			for (String columnName : tuple.getColNames())
			{
				Object value = tuple.get(columnName);
				if (value instanceof Collection)
				{
					value = Joiner.on(" , ").join((Collection<?>) value);
				}

				doc.put(columnName, value);

				// doc.put(columnName, tuple.get(columnName));
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
			LOG.info("Added [" + (++count) + "] documents");
		}

		return bulkRequest;
	}
}
