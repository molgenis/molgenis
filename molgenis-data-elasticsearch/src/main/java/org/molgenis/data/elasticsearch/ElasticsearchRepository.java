package org.molgenis.data.elasticsearch;

import static org.molgenis.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheResponse;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.index.MappingsBuilder;
import org.molgenis.elasticsearch.request.LuceneQueryStringBuilder;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class ElasticsearchRepository implements CrudRepository, Manageable
{
	public static final String BASE_URL = "elasticsearch://";

	private static final int BULK_SIZE = 1000;

	private final Client client;
	private final String indexName;
	private final String docType;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;

	public ElasticsearchRepository(Client client, String indexName, EntityMetaData entityMetaData,
			DataService dataService)
	{
		if (client == null) throw new IllegalArgumentException("client is null");
		if (indexName == null) throw new IllegalArgumentException("indexName is null");
		if (entityMetaData == null) throw new IllegalArgumentException("entityMetaData is null");
		this.client = client;
		this.indexName = indexName;
		this.entityMetaData = entityMetaData;
		this.docType = sanitizeMapperType(entityMetaData.getName());
		this.dataService = dataService;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + docType + '/';
	}

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}

	@Override
	public long count()
	{
		CountResponse countResponse = createCountRequest(null).execute().actionGet();
		if (countResponse.getFailedShards() > 0)
		{
			throw new MolgenisDataException("An error occured counting entities");
		}
		return countResponse.getCount();
	}

    @Override
    public Query query() {
        return new QueryImpl(this);
    }

    @Override
	public long count(Query q)
	{
		CountResponse countResponse = createCountRequest(q).execute().actionGet();
		if (countResponse.getFailedShards() > 0)
		{
			throw new MolgenisDataException("An error occured counting entities");
		}
		return countResponse.getCount();
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		SearchResponse searchResponse = createSearchRequest(q).execute().actionGet();
		SearchHits searchHits = searchResponse.getHits();
		return Iterables.transform(searchHits, new Function<SearchHit, Entity>()
		{
			@Override
			public Entity apply(SearchHit searchHit)
			{
				return new ElasticsearchEntity(searchHit, getEntityMetaData(), dataService);
			}
		});
	}

	@Override
	public Entity findOne(Query q)
	{
		SearchResponse searchResponse = createSearchRequest(q).setSize(1).execute().actionGet();
		SearchHits searchHits = searchResponse.getHits();
		SearchHit searchHit = searchHits.getHits()[0];
		return new ElasticsearchEntity(searchHit, getEntityMetaData(), dataService);
	}

	@Override
	public Entity findOne(Object id)
	{
		GetResponse getResponse = client.prepareGet(indexName, docType, id.toString()).execute().actionGet();
		if (getResponse.isExists())
		{
			return new ElasticsearchEntity(getResponse.getId(), getResponse.getSource(), getEntityMetaData(),
					dataService);
		}
		else
		{
			return null;
		}
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		final MultiGetResponse multiGetResponse = client.prepareMultiGet()
				.add(indexName, docType, Iterables.transform(ids, new Function<Object, String>()
				{
					@Override
					public String apply(Object obj)
					{
						return obj.toString();
					}
				})).execute().actionGet();

		return new Iterable<Entity>()
		{

			@Override
			public Iterator<Entity> iterator()
			{
				return Iterators.transform(multiGetResponse.iterator(), new Function<MultiGetItemResponse, Entity>()
				{

					@Override
					public Entity apply(MultiGetItemResponse multiGetItemResponse)
					{
						if (multiGetItemResponse.isFailed())
						{
							throw new MolgenisDataException("An error occured finding entities");
						}
						GetResponse response = multiGetItemResponse.getResponse();
						return new ElasticsearchEntity(response.getId(), response.getSource(), getEntityMetaData(),
								dataService);
					}
				});
			}
		};
	}

	@Override
	public Iterator<Entity> iterator()
	{
		SearchResponse searchResponse = createSearchRequest(null).setSize(Integer.MAX_VALUE).execute().actionGet();
		SearchHits searchHits = searchResponse.getHits();
		return Iterators.transform(searchHits.iterator(), new Function<SearchHit, Entity>()
		{
			@Override
			public Entity apply(SearchHit searchHit)
			{
				return new ElasticsearchEntity(searchHit, getEntityMetaData(), dataService);
			}
		});
	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttr, AttributeMetaData yAttr, Query q)
	{
		String xAttrName = xAttr != null ? xAttr.getName() : null;
		String yAttrName = yAttr != null ? yAttr.getName() : null;
		if (xAttrName == null && yAttrName == null)
		{
			throw new MolgenisDataException("Expected at least one aggregation attribute");
		}

		// create individual aggregation queries
		TermsBuilder xTermsBuilder = null, yTermsBuilder = null;
		if (xAttrName != null)
		{
			xTermsBuilder = new TermsBuilder(xAttrName).size(Integer.MAX_VALUE).field(xAttrName);
		}
		if (yAttrName != null)
		{
			yTermsBuilder = new TermsBuilder(yAttrName).size(Integer.MAX_VALUE).field(yAttrName);
		}

		// create combined aggregation query
		TermsBuilder termsBuilder;
		if (xTermsBuilder != null)
		{
			termsBuilder = xTermsBuilder;
			if (yTermsBuilder != null)
			{
				termsBuilder.subAggregation(yTermsBuilder);
			}
		}
		else
		{
			termsBuilder = yTermsBuilder;
		}

		SearchResponse searchResponse = createSearchRequest(q).setSize(0).addAggregation(termsBuilder).execute()
				.actionGet();

		Aggregations aggregations = searchResponse.getAggregations();

		// create aggregate result
		List<List<Long>> matrix = Lists.newArrayList();
		Set<String> xLabelsSet = Sets.newHashSet();
		Set<String> yLabelsSet = Sets.newHashSet();
		List<String> xLabels = new ArrayList<String>();
		List<String> yLabels = new ArrayList<String>();
		final int nrAggregations = Iterables.size(aggregations);
		if (nrAggregations != 1)
		{
			throw new RuntimeException("Multiple aggregations [" + nrAggregations + "] not supported");
		}

		Aggregation aggregation = aggregations.iterator().next();
		if (!(aggregation instanceof Terms))
		{
			throw new RuntimeException("Aggregation of type [" + aggregation.getClass().getName() + "] not supported");
		}
		Terms terms = (Terms) aggregation;

		Collection<Bucket> buckets = terms.getBuckets();
		if (buckets.size() > 0)
		{
			// distinguish between 1D and 2D aggregation
			boolean hasSubAggregations = false;
			for (Bucket bucket : buckets)
			{
				Aggregations subAggregations = bucket.getAggregations();
				if (subAggregations != null && Iterables.size(subAggregations) > 0)
				{
					hasSubAggregations = true;
					break;
				}
			}

			if (hasSubAggregations)
			{
				// create labels
				for (Bucket bucket : buckets)
				{
					if (!xLabelsSet.contains(bucket.getKey())) xLabelsSet.add(bucket.getKey());

					Aggregations subAggregations = bucket.getAggregations();
					if (subAggregations != null)
					{
						if (Iterables.size(subAggregations) > 1)
						{
							throw new RuntimeException("Multiple aggregations [" + nrAggregations + "] not supported");
						}
						Aggregation subAggregation = subAggregations.iterator().next();

						if (!(subAggregation instanceof Terms))
						{
							throw new RuntimeException("Aggregation of type [" + subAggregation.getClass().getName()
									+ "] not supported");
						}
						Terms subTerms = (Terms) subAggregation;

						for (Bucket subBucket : subTerms.getBuckets())
						{
							yLabelsSet.add(subBucket.getKey());
						}

					}
				}

				xLabels = new ArrayList<String>(xLabelsSet);
				Collections.sort(xLabels);
				xLabels.add("Total");
				yLabels = new ArrayList<String>(yLabelsSet);
				Collections.sort(yLabels);
				yLabels.add("Total");

				// create label map
				int idx = 0;
				Map<String, Integer> yLabelMap = new HashMap<String, Integer>();
				for (String yLabel : yLabels)
				{
					yLabelMap.put(yLabel, idx++);
				}

				for (Bucket bucket : buckets)
				{
					// create values
					List<Long> yValues = new ArrayList<Long>();
					for (int i = 0; i < idx; ++i)
					{
						yValues.add(0l);
					}

					Aggregations subAggregations = bucket.getAggregations();
					if (subAggregations != null)
					{
						long count = 0;
						Terms subTerms = (Terms) subAggregations.iterator().next();
						for (Bucket subBucket : subTerms.getBuckets())
						{
							long bucketCount = subBucket.getDocCount();
							yValues.set(yLabelMap.get(subBucket.getKey()), bucketCount);
							count += bucketCount;
						}
						yValues.set(yLabelMap.get("Total"), count);
					}

					matrix.add(yValues);
				}

				// create value totals
				List<Long> xTotals = new ArrayList<Long>();
				for (int i = 0; i < idx; ++i)
				{
					xTotals.add(0l);
				}

				for (List<Long> values : matrix)
				{
					int nrValues = values.size();
					for (int i = 0; i < nrValues; ++i)
					{
						xTotals.set(i, xTotals.get(i) + values.get(i));
					}
				}
				matrix.add(xTotals);
			}
			else
			{
				long total = 0;
				for (Bucket bucket : buckets)
				{
					xLabels.add(bucket.getKey());
					long docCount = bucket.getDocCount();
					matrix.add(Lists.newArrayList(Long.valueOf(docCount)));
					total += docCount;
				}
				matrix.add(Lists.newArrayList(Long.valueOf(total)));
				xLabels.add("Total");
				yLabels.add("Count");
			}
		}

		return new AggregateResult(matrix, xLabels, yLabels);
	}

	@Override
	public void add(Entity entity)
	{
		createIndexRequest(entity).execute().actionGet();
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		BulkRequestBuilder bulkRequestBuilder = null;
		int count = 0;
		for (Entity entity : entities)
		{
			IndexRequestBuilder indexRequestBuilder = createIndexRequest(entity);
			if (bulkRequestBuilder == null) bulkRequestBuilder = client.prepareBulk();
			bulkRequestBuilder.add(indexRequestBuilder);

			if (++count % BULK_SIZE == 0)
			{
				BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
				if (bulkResponse.hasFailures())
				{
					throw new MolgenisDataException("An error occured adding entities");
				}
				bulkRequestBuilder = null;
			}
		}
		if (bulkRequestBuilder != null)
		{
			BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
			if (bulkResponse.hasFailures())
			{
				throw new MolgenisDataException("An error occured adding entities");
			}
		}
		return count;
	}

	@Override
	public void update(Entity entity)
	{
		createUpdateRequest(entity).execute().actionGet();
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		BulkRequestBuilder bulkRequestBuilder = null;
		int count = 0;
		for (Entity entity : entities)
		{
			if (bulkRequestBuilder == null) bulkRequestBuilder = client.prepareBulk();
			UpdateRequestBuilder updateRequestBuilder = createUpdateRequest(entity);
			bulkRequestBuilder.add(updateRequestBuilder);

			if (++count % BULK_SIZE == 0)
			{
				BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
				if (bulkResponse.hasFailures())
				{
					throw new MolgenisDataException("An error occured updating entities");
				}
				bulkRequestBuilder = null;
			}
		}
		if (bulkRequestBuilder != null)
		{
			BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
			if (bulkResponse.hasFailures())
			{
				throw new MolgenisDataException("An error occured updating entities");
			}
		}
	}

	@Override
	public void delete(Entity entity)
	{
		DeleteResponse deleteResponse = createDeleteRequest(entity).execute().actionGet();
		if (!deleteResponse.isFound())
		{
			throw new MolgenisDataException("Failed to delete entity, entity does not exist [" + entity + "]");
		}
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		BulkRequestBuilder bulkRequestBuilder = null;
		int count = 0;
		for (Entity entity : entities)
		{
			if (bulkRequestBuilder == null) bulkRequestBuilder = client.prepareBulk();
			DeleteRequestBuilder deleteRequestBuilder = createDeleteRequest(entity);
			bulkRequestBuilder.add(deleteRequestBuilder);

			if (++count % BULK_SIZE == 0)
			{
				BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
				if (bulkResponse.hasFailures())
				{
					throw new MolgenisDataException("An error occured adding entities");
				}
				bulkRequestBuilder = null;
			}
		}
		if (bulkRequestBuilder != null)
		{
			BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
			if (bulkResponse.hasFailures())
			{
				throw new MolgenisDataException("An error occured adding entities");
			}
		}
	}

	@Override
	public void deleteById(Object id)
	{
		DeleteResponse deleteResponse = createDeleteRequest(id).execute().actionGet();
		if (!deleteResponse.isFound())
		{
			throw new MolgenisDataException("Failed to delete entity, entity with id [" + id + "] does not exist");
		}
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		BulkRequestBuilder bulkRequestBuilder = null;
		int count = 0;
		for (Object id : ids)
		{
			if (bulkRequestBuilder == null) bulkRequestBuilder = client.prepareBulk();
			DeleteRequestBuilder deleteRequestBuilder = createDeleteRequest(id);
			bulkRequestBuilder.add(deleteRequestBuilder);

			if (++count % BULK_SIZE == 0)
			{
				BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
				if (bulkResponse.hasFailures())
				{
					throw new MolgenisDataException("An error occured deleting entities");
				}
				bulkRequestBuilder = null;
			}
		}
		if (bulkRequestBuilder != null)
		{
			BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
			if (bulkResponse.hasFailures())
			{
				throw new MolgenisDataException("An error occured deleting entities");
			}
		}
	}

	@Override
	public void deleteAll()
	{
		DeleteByQueryResponse deleteByQueryResponse = client.prepareDeleteByQuery(indexName).setTypes(docType)
				.execute().actionGet();
		for (IndexDeleteByQueryResponse indexDeleteByQueryResponse : deleteByQueryResponse)
		{
			if (indexDeleteByQueryResponse.getFailedShards() > 0) throw new MolgenisDataException(
					"An error occured deleting entities");
		}
	}

	@Override
	public void close() throws IOException
	{
		// no operation
	}

	@Override
	public void flush()
	{
		FlushResponse flushResponse = client.admin().indices().prepareFlush(indexName).execute().actionGet();
		if (flushResponse.getFailedShards() > 0)
		{
			throw new MolgenisDataException("An error occured flushing repository");
		}
	}

	@Override
	public void clearCache()
	{
		ClearIndicesCacheResponse clearIndicesCacheResponse = client.admin().indices().prepareClearCache(indexName)
				.execute().actionGet();
		if (clearIndicesCacheResponse.getFailedShards() > 0)
		{
			throw new MolgenisDataException("An error occured clearing repository cache");
		}
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		if (!clazz.isAssignableFrom(ElasticsearchEntity.class))
		{
			throw new MolgenisDataException("Unsupported entity class + [" + clazz.getName() + "]");
		}
		return (Iterable<E>) findAll(ids);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		if (!clazz.isAssignableFrom(ElasticsearchEntity.class))
		{
			throw new MolgenisDataException("Unsupported entity class + [" + clazz.getName() + "]");
		}
		return (E) findOne(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		if (!clazz.isAssignableFrom(ElasticsearchEntity.class))
		{
			throw new MolgenisDataException("Unsupported entity class + [" + clazz.getName() + "]");
		}
		return (E) findOne(q);
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		// FIXME remove this update method
		throw new UnsupportedOperationException();
	}

	private QueryBuilder createQuery(List<QueryRule> queryRules)
	{
		String queryString = LuceneQueryStringBuilder.buildQueryString(queryRules);
		return QueryBuilders.queryString(queryString).defaultOperator(Operator.AND);
	}

	private Map<String, Object> createSource(Entity entity)
	{
		Map<String, Object> doc = new HashMap<String, Object>();

		EntityMetaData entityMetaData = getEntityMetaData();
		for (AttributeMetaData attribute : entityMetaData.getAttributes())
		{
			updateSource(entity, attribute, doc);
		}
		return doc;
	}

	private void updateSource(Entity entity, AttributeMetaData attribute, Map<String, Object> doc)
	{
		String attrName = attribute.getName();
		FieldTypeEnum dataType = attribute.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
			{
				doc.put(attrName, entity.getBoolean(attrName));
				break;
			}
			case CATEGORICAL:
			case XREF:
			{
				Entity refEntity = entity.getEntity(attrName);
				doc.put(attrName, refEntity.getLabelValue());
				doc.put(attrName + ".id", refEntity.getIdValue());
				break;
			}
			case COMPOUND:
			{
				for (AttributeMetaData refAttribute : attribute.getAttributeParts())
				{
					updateSource(entity, refAttribute, doc);
				}
				break;
			}
			case DATE:
			{
				Date date = entity.getDate(attrName);
				doc.put(attrName, new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATE).format(date));
				break;
			}
			case DATE_TIME:
			{
				Timestamp timestamp = entity.getTimestamp(attrName);
				doc.put(attrName, new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME).format(timestamp));
				break;
			}
			case DECIMAL:
			{
				doc.put(attrName, entity.getDouble(attrName));
			}
			case EMAIL:
			case ENUM:
			case FILE:
			case HTML:
			case HYPERLINK:
			case IMAGE:
			case STRING:
			case TEXT:
			{
				doc.put(attrName, entity.getString(attrName));
				break;
			}
			case INT:
			{
				doc.put(attrName, entity.getInt(attrName));
				break;
			}
			case LONG:
			{
				doc.put(attrName, entity.getLong(attrName));
				break;
			}
			case MREF:
			{
				List<String> ids = null, values = null;
				Iterable<Entity> entities = entity.getEntities(attrName);
				if (entities != null)
				{
					for (Entity refEntity : entities)
					{
						if (ids == null) ids = new ArrayList<String>();
						ids.add(refEntity.getIdValue().toString());
						if (values == null) values = new ArrayList<String>();
						values.add(refEntity.getLabelValue());
					}
					if (ids != null && values != null)
					{
						doc.put(attrName, values);
						doc.put(attrName + ".id", ids);
					}
				}
				break;
			}
			default:
				throw new RuntimeException("Unknown attribute data type [" + dataType + "]");

		}
	}

	private CountRequestBuilder createCountRequest(Query q)
	{
		CountRequestBuilder countRequestBuilder = client.prepareCount(indexName).setTypes(docType);
		if (q != null && q.getRules() != null && !q.getRules().isEmpty())
		{
			QueryBuilder queryBuilder = createQuery(q.getRules());
			countRequestBuilder.setQuery(queryBuilder);
		}
		return countRequestBuilder;
	}

	private SearchRequestBuilder createSearchRequest(Query q)
	{
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(docType);
		if (q != null)
		{
			if (q.getRules() != null && !q.getRules().isEmpty())
			{
				QueryBuilder queryBuilder = createQuery(q.getRules());
				searchRequestBuilder.setQuery(queryBuilder);
			}

			Sort sort = q.getSort();
			if (sort != null)
			{
				for (Order order : sort)
				{
					String field = order.getProperty();
					SortOrder sortOrder = order.getDirection() == Direction.ASC ? SortOrder.ASC : SortOrder.DESC;
					searchRequestBuilder.addSort(field, sortOrder);
				}
			}

			int offset = q.getOffset();
			int pageSize = q.getPageSize();
			if (!(offset == 0 && pageSize == 0))
			{
				searchRequestBuilder.setFrom(offset).setSize(pageSize);
			}
		}
		return searchRequestBuilder;
	}

	private IndexRequestBuilder createIndexRequest(Entity entity)
	{
		Map<String, Object> source = createSource(entity);
		return client.prepareIndex(indexName, docType).setSource(source);
	}

	private UpdateRequestBuilder createUpdateRequest(Entity entity)
	{
		String id = entity.getIdValue().toString();
		Map<String, Object> source = createSource(entity);
		return client.prepareUpdate(indexName, docType, id).setDoc(source);
	}

	private DeleteRequestBuilder createDeleteRequest(Entity entity)
	{
		return createDeleteRequest(entity.getIdValue());
	}

	private DeleteRequestBuilder createDeleteRequest(Object id)
	{
		return client.prepareDelete(indexName, docType, id.toString());
	}

	@Override
	public void create()
	{
		if (!MappingsBuilder.hasMapping(client, entityMetaData))
		{
			try
			{
				MappingsBuilder.createMapping(client, entityMetaData);
			}
			catch (IOException e)
			{
				throw new MolgenisDataException(e);
			}
		}
	}

	@Override
	public void drop()
	{
		String documentTypeSantized = sanitizeMapperType(docType);

		DeleteByQueryResponse deleteResponse = client.prepareDeleteByQuery(indexName)
				.setQuery(new TermQueryBuilder("_type", documentTypeSantized)).execute().actionGet();

		if (deleteResponse != null)
		{
			IndexDeleteByQueryResponse idbqr = deleteResponse.getIndex(indexName);
			if ((idbqr != null) && (idbqr.getFailedShards() > 0))
			{
				throw new ElasticsearchException("Delete failed. Returned headers:" + idbqr.getHeaders());
			}
		}
	}
}