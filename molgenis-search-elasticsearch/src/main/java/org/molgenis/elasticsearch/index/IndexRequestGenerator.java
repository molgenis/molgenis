package org.molgenis.elasticsearch.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.util.DatabaseUtil;
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
			if (entity instanceof DataSet)
			{
				buildIndexRequestForProtocol(documentName + '-' + entity.getIdValue(),
						((DataSet) entity).getProtocolUsed(), bulkRequest);
			}
			else
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
		}

		return bulkRequest;
	}

	private void buildIndexRequestForProtocol(String documentName, Protocol entity, BulkRequestBuilder bulkRequest)
	{
		Database db = DatabaseUtil.createDatabase();

		try
		{
			buildIndexRequestForProtocolRec(documentName, "", entity, db, bulkRequest);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			DatabaseUtil.closeQuietly(db);
		}

	}

	private void buildIndexRequestForProtocolRec(String documentName, String protocolPath, Protocol protocol,
			Database db, BulkRequestBuilder bulkRequest) throws DatabaseException
	{

		List<Integer> protocolIds = protocol.getSubprotocols_Id();

		if (protocolIds.size() > 0)
		{
			List<Protocol> subProtocols = db.find(Protocol.class, new QueryRule(Protocol.ID, Operator.IN, protocolIds));
			for (Protocol p : subProtocols)
			{
				StringBuilder pathBuilder = new StringBuilder();
				if (!protocolPath.isEmpty()) pathBuilder.append(protocolPath).append('.');
				String name = p.getName().replaceAll("[^a-zA-Z0-9 ]", " ");
				final String path = pathBuilder.append(p.getId()).toString();
				String description = p.getDescription() == null ? StringUtils.EMPTY : p.getDescription().replaceAll(
						"[^a-zA-Z0-9 ]", " ");

				Map<String, Object> doc = new HashMap<String, Object>();
				doc.put("path", path);
				doc.put("id", p.getId());
				doc.put("name", name);
				doc.put("type", "protocol");
				doc.put("description", description);

				IndexRequestBuilder request = client.prepareIndex(indexName, documentName, p.getIdValue() + "");
				request.setSource(doc);
				bulkRequest.add(request);

				// recursively traverse down the tree
				buildIndexRequestForProtocolRec(documentName, pathBuilder.toString(), p, db, bulkRequest);

			}
		}
		else
		{
			List<Integer> featureIds = protocol.getFeatures_Id();
			if (featureIds.size() > 0)
			{
				List<ObservableFeature> listOfFeatures = db.find(ObservableFeature.class, new QueryRule(
						ObservableFeature.ID, Operator.IN, featureIds));
				for (ObservableFeature feature : listOfFeatures)
				{
					StringBuilder pathBuilder = new StringBuilder();
					String name = feature.getName().replaceAll("[^a-zA-Z0-9 ]", " ");
					String description = feature.getDescription() == null ? StringUtils.EMPTY : feature
							.getDescription().replaceAll("[^a-zA-Z0-9 ]", " ");
					String path = pathBuilder.append(protocolPath).append(".F").append(feature.getId()).toString();
					StringBuilder categoryValue = new StringBuilder();

					for (Category c : Category.find(db, new QueryRule(Category.OBSERVABLEFEATURE_IDENTIFIER,
							Operator.EQUALS, feature.getIdentifier())))
					{
						String categoryName = c.getName() == null ? StringUtils.EMPTY : c.getName().replaceAll(
								"[^a-zA-Z0-9 ]", " ");
						categoryValue.append(categoryName).append(' ');
					}

					Map<String, Object> doc = new HashMap<String, Object>();
					doc.put("id", feature.getId());
					doc.put("name", name);
					doc.put("type", "observablefeature");
					doc.put("description", description);
					doc.put("path", path);
					doc.put("category", categoryValue.toString());

					IndexRequestBuilder request = client.prepareIndex(indexName, documentName, feature.getIdValue()
							+ "");
					request.setSource(doc);
					bulkRequest.add(request);
				}
			}
		}
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
				doc.put(columnName, tuple.get(columnName));
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
