package org.molgenis.data.elasticsearch;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.elasticsearch.util.ElasticsearchClientFacade;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.BatchingIterable;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Retrieve search results in batches. Note: We do not use Elasticsearch scan & scroll, because scrolling is not
 * intended for real time user request: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current
 * /search-request-scroll.html
 */
class ElasticsearchDocumentIdIterable extends BatchingIterable<String>
{
	private static final int BATCH_SIZE = 1000;

	private final Query<Entity> query;
	private final EntityType entityType;
	private final ElasticsearchClientFacade elasticsearchFacade;
	private final SearchRequestGenerator searchRequestGenerator;
	private final String indexName;
	private final String documentType;

	ElasticsearchDocumentIdIterable(Query<Entity> query, EntityType entityType,
			ElasticsearchClientFacade elasticsearchFacade, SearchRequestGenerator searchRequestGenerator,
			String indexName, String documentType)
	{
		super(BATCH_SIZE, query.getOffset(), query.getPageSize());
		this.query = requireNonNull(query);
		this.entityType = requireNonNull(entityType);
		this.elasticsearchFacade = requireNonNull(elasticsearchFacade);
		this.searchRequestGenerator = requireNonNull(searchRequestGenerator);
		this.indexName = requireNonNull(indexName);
		this.documentType = requireNonNull(documentType);
	}

	@Override
	protected List<String> getBatch(int offset, int batchSize)
	{
		Query<Entity> batchQuery = createBatchQuery(offset, batchSize);
		return executeSearch(batchQuery).collect(toList());
	}

	private Query<Entity> createBatchQuery(int offset, int batchSize)
	{
		Query<Entity> batchQuery;
		if (offset != query.getOffset() || batchSize != query.getPageSize())
		{
			batchQuery = new QueryImpl<>(query).setOffset(offset).setPageSize(batchSize);
		}
		else
		{
			batchQuery = query;
		}
		return batchQuery;
	}

	private Stream<String> executeSearch(Query<Entity> query)
	{
		Consumer<SearchRequestBuilder> searchRequestBuilderConsumer = searchRequestBuilder -> searchRequestGenerator
				.buildSearchRequest(searchRequestBuilder, SearchType.QUERY_THEN_FETCH, entityType, query, null, null,
						null);
		return elasticsearchFacade
				.searchForIds(searchRequestBuilderConsumer, query.toString(), documentType, indexName);
	}
}
