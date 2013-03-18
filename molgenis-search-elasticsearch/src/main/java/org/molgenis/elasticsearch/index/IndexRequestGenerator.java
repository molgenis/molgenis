package org.molgenis.elasticsearch.index;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.molgenis.framework.tupletable.TupleTable;
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

		int count = 0;
		for (Tuple tuple : tupleTable)
		{
			Map<String, Object> doc = new HashMap<String, Object>();
			for (String columnName : tuple.getColNames())
			{
				doc.put(columnName, tuple.get(columnName));
			}

			IndexRequestBuilder request = client.prepareIndex(indexName, documentName);

			request.setSource(doc);
			bulkRequest.add(request);
			LOG.info("Added [" + (++count) + "] documents");
		}

		return bulkRequest;
	}

}
