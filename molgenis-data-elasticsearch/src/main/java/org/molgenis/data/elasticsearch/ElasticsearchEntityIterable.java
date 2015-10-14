package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static org.elasticsearch.index.query.FilterBuilders.queryFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.indicesQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchService.CrudType;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Retrieve search results in batches. Note: We do not use Elasticsearch scan & scroll, because scrolling is not
 * intended for real time user request: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current
 * /search-request-scroll.html
 */
class ElasticsearchEntityIterable implements Iterable<Entity>
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchEntityIterable.class);

	private static final int BATCH_SIZE = 1000;

	private final Query q;
	private final EntityMetaData entityMeta;
	private final Client client;
	private final DataService dataService;;
	private final SearchRequestGenerator searchRequestGenerator;
	private final String[] indexNames;

	private final String type;
	private final List<String> fieldsToReturn;
	private final int offset;
	private final int pageSize;

	public ElasticsearchEntityIterable(Query q, EntityMetaData entityMetaData, Client client, DataService dataService,
			SearchRequestGenerator searchRequestGenerator, String[] indexNames)
	{
		this.q = requireNonNull(q);
		this.entityMeta = requireNonNull(entityMetaData);
		this.client = requireNonNull(client);
		this.dataService = requireNonNull(dataService);
		this.searchRequestGenerator = requireNonNull(searchRequestGenerator);
		this.indexNames = requireNonNull(indexNames);

		this.type = sanitizeMapperType(entityMetaData.getName());
		this.fieldsToReturn = Collections.<String> emptyList();
		this.offset = q.getOffset();
		this.pageSize = q.getPageSize();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new Iterator<Entity>()
		{
			private long totalHits;
			private List<Entity> batchEntities;
			private int batchPos;

			private int currentOffset;

			@Override
			public boolean hasNext()
			{
				if (batchEntities == null)
				{
					int batchOffset = offset;
					int batchSize = pageSize != 0 ? Math.min(pageSize - currentOffset, BATCH_SIZE) : BATCH_SIZE;
					doBatchSearch(batchOffset, batchSize);
				}

				if (batchEntities.isEmpty())
				{
					return false;
				}

				if (batchPos < batchEntities.size())
				{
					return true;
				}
				else if (batchPos == batchEntities.size())
				{
					long requestedHits = pageSize != 0 ? Math.min(pageSize, totalHits) : totalHits;
					if (currentOffset + batchEntities.size() < requestedHits)
					{
						int batchOffset = currentOffset + BATCH_SIZE;
						int batchSize = pageSize != 0 ? Math.min(pageSize - batchOffset, BATCH_SIZE) : BATCH_SIZE;
						doBatchSearch(batchOffset, batchSize);

						return !batchEntities.isEmpty();
					}
					else
					{
						return false;
					}
				}
				else throw new RuntimeException();
			}

			@Override
			public Entity next()
			{
				boolean next = hasNext();
				if (next)
				{
					Entity entity = batchEntities.get(batchPos);
					++batchPos;
					return entity;
				}
				else throw new ArrayIndexOutOfBoundsException();
			}

			private void doBatchSearch(int from, int size)
			{
				q.offset(from);
				q.pageSize(size);

				if (LOG.isTraceEnabled())
				{
					LOG.trace("Searching Elasticsearch '" + type + "' docs using query [" + q + "] ...");
				}

				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexNames);
				searchRequestGenerator.buildSearchRequest(searchRequestBuilder, type, SearchType.QUERY_AND_FETCH, q,
						fieldsToReturn, null, null, null, entityMeta);

				// We are in a transaction, the first index is the status before the transaction started, the second
				// index the status within the transaction. We don't want to return the deleted records and of the
				// updated records we want the latest version (that of the transaction)
				if (indexNames.length > 1)
				{
					QueryBuilder findUpdatesQuery = indicesQuery(
							termQuery(ElasticsearchService.CRUD_TYPE_FIELD_NAME, CrudType.UPDATE.name()),
							indexNames[1]);

					// Exclude the updated records from the first index
					QueryBuilder excludeUpdatesQuery = indicesQuery(boolQuery().mustNot(findUpdatesQuery),
							indexNames[0]);

					// NOTE: deletes cannot be handled by ES in this way, so if you do a delete then the entity will
					// still be returned. Only after the commit of the transaction the queries won't return the
					// entity anymore

					searchRequestBuilder.setPostFilter(queryFilter(excludeUpdatesQuery));
				}

				if (LOG.isTraceEnabled())
				{
					LOG.trace("SearchRequest: " + searchRequestBuilder);
				}
				SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

				if (searchResponse.getFailedShards() > 0)
				{
					StringBuilder sb = new StringBuilder("Search failed.");
					for (ShardSearchFailure failure : searchResponse.getShardFailures())
					{
						sb.append("\n").append(failure.reason());
					}
					throw new ElasticsearchException(sb.toString());
				}
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Searched Elasticsearch '" + type + "' docs using query [" + q + "] in "
							+ searchResponse.getTookInMillis() + "ms");
				}
				SearchHits searchHits = searchResponse.getHits();
				this.totalHits = searchHits.getTotalHits();
				SearchHit[] batchHits = searchHits.getHits();
				if (batchHits.length > 0)
				{
					List<Object> entityIds = Arrays.stream(batchHits).map(SearchHit::getId)
							.collect(Collectors.toList());
					this.batchEntities = Lists.newArrayList(dataService.findAll(entityMeta.getName(), entityIds));
				}
				else
				{
					this.batchEntities = Collections.emptyList();
				}
				this.batchPos = 0;

				this.currentOffset = from;
			}
		};
	}
}