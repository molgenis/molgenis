package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static org.elasticsearch.index.query.FilterBuilders.queryFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.indicesQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.molgenis.data.DataConverter.convert;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchService.CrudType;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.support.BatchingQueryResult;
import org.molgenis.data.support.EntityMetaDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Retrieve search results in batches. Note: We do not use Elasticsearch scan & scroll, because scrolling is not
 * intended for real time user request: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current
 * /search-request-scroll.html
 */
class ElasticsearchEntityIterable extends BatchingQueryResult implements EntityCollection
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchEntityIterable.class);

	private static final int BATCH_SIZE = 1000;

	private final EntityMetaData entityMeta;
	private final Client client;
	private final ElasticsearchEntityFactory elasticsearchEntityFactory;
	private final SearchRequestGenerator searchRequestGenerator;
	private final String[] indexNames;

	private final String type;

	public ElasticsearchEntityIterable(Query q, EntityMetaData entityMetaData, Client client,
			ElasticsearchEntityFactory elasticsearchEntityFactory, SearchRequestGenerator searchRequestGenerator,
			String[] indexNames)
	{
		super(BATCH_SIZE, q);
		this.entityMeta = requireNonNull(entityMetaData);
		this.client = requireNonNull(client);
		this.elasticsearchEntityFactory = requireNonNull(elasticsearchEntityFactory);
		this.searchRequestGenerator = requireNonNull(searchRequestGenerator);
		this.indexNames = requireNonNull(indexNames);

		this.type = sanitizeMapperType(entityMetaData.getName());
	}

	@Override
	protected List<Entity> getBatch(Query q)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("Searching Elasticsearch '" + type + "' docs using query [" + q + "] ...");
		}

		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexNames);
		searchRequestGenerator.buildSearchRequest(searchRequestBuilder, type, SearchType.QUERY_AND_FETCH, q, null, null,
				null, entityMeta);

		// We are in a transaction, the first index is the status before the transaction started, the second
		// index the status within the transaction. We don't want to return the deleted records and of the
		// updated records we want the latest version (that of the transaction)
		if (indexNames.length > 1)
		{
			QueryBuilder findUpdatesQuery = indicesQuery(
					termQuery(ElasticsearchService.CRUD_TYPE_FIELD_NAME, CrudType.UPDATE.name()), indexNames[1]);

			// Exclude the updated records from the first index
			QueryBuilder excludeUpdatesQuery = indicesQuery(boolQuery().mustNot(findUpdatesQuery), indexNames[0]);

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

		List<Entity> entities;
		if (searchHits.hits().length > 0)
		{
			if (ElasticsearchRepositoryCollection.NAME.equals(entityMeta.getBackend()))
			{
				// create entities from the source documents
				entities = StreamSupport.stream(searchHits.spliterator(), false).map(
						searchHit -> elasticsearchEntityFactory.create(entityMeta, searchHit.getSource(), q.getFetch()))
						.collect(Collectors.toList());
			}
			else
			{
				// create entity references for the search result document ids
				entities = Lists.newArrayList(createEntityReferences(searchHits));
			}
		}
		else
		{
			entities = Collections.emptyList();
		}
		return entities;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return EntityMetaDataUtils.getAttributeNames(entityMeta.getAtomicAttributes());
	}

	@Override
	public boolean isLazy()
	{
		return !ElasticsearchRepositoryCollection.NAME.equals(entityMeta.getBackend());
	}

	private Iterable<Entity> createEntityReferences(SearchHits searchHits)
	{
		// create entity references for the search result document ids
		return elasticsearchEntityFactory.getEntityManager().getReferences(entityMeta, new Iterable<Object>()
		{
			@Override
			public Iterator<Object> iterator()
			{
				// convert id value to required id data type (Elasticsearch ids are always string)
				return StreamSupport.stream(searchHits.spliterator(), false).map(SearchHit::getId)
						.map(idString -> convert(idString, entityMeta.getIdAttribute())).collect(Collectors.toList())
						.iterator();
			}
		});
	}
}