package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.molgenis.data.DataConverter.convert;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.elasticsearch.util.ElasticsearchUtils;
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
class ElasticsearchEntityIterable extends BatchingQueryResult<Entity> implements EntityCollection
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchEntityIterable.class);

	private static final int BATCH_SIZE = 1000;

	private final EntityMetaData entityMeta;
	private final ElasticsearchEntityFactory elasticsearchEntityFactory;
	private final SearchRequestGenerator searchRequestGenerator;
	private final String indexName;

	private final String type;
	private final ElasticsearchUtils elasticsearchFacade;

	ElasticsearchEntityIterable(Query<Entity> q, EntityMetaData entityMetaData,
			ElasticsearchUtils elasticsearchFacade,
			ElasticsearchEntityFactory elasticsearchEntityFactory, SearchRequestGenerator searchRequestGenerator,
			String indexName)
	{
		super(BATCH_SIZE, q);
		this.entityMeta = requireNonNull(entityMetaData);
		this.elasticsearchFacade = requireNonNull(elasticsearchFacade);
		this.elasticsearchEntityFactory = requireNonNull(elasticsearchEntityFactory);
		this.searchRequestGenerator = requireNonNull(searchRequestGenerator);
		this.indexName = requireNonNull(indexName);

		this.type = sanitizeMapperType(entityMetaData.getName());
	}

	@Override
	protected List<Entity> getBatch(Query<Entity> q)
	{
		SearchHits searchHits = elasticsearchFacade.search((searchRequestBuilder) -> searchRequestGenerator
				.buildSearchRequest(searchRequestBuilder, type, SearchType.QUERY_AND_FETCH, q, null, null, null,
						entityMeta), q.toString(), type, indexName);

		List<Entity> entities;
		if (searchHits.hits().length > 0)
		{
			if (ElasticsearchRepositoryCollection.NAME.equals(entityMeta.getBackend()))
			{
				// create entities from the source documents
				entities = StreamSupport.stream(searchHits.spliterator(), false)
						.map(searchHit -> elasticsearchEntityFactory
								.create(entityMeta, searchHit.getSource(), q.getFetch())).collect(Collectors.toList());
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
		return elasticsearchEntityFactory.getEntityManager().getReferences(entityMeta, () -> {
			// convert id value to required id data type (Elasticsearch ids are always string)
			return StreamSupport.stream(searchHits.spliterator(), false).map(SearchHit::getId)
					.map(idString -> convert(idString, entityMeta.getIdAttribute())).collect(Collectors.toList())
					.iterator();
		});
	}
}