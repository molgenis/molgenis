package org.molgenis.data.elasticsearch;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.request.SearchRequestGenerator;
import org.molgenis.data.elasticsearch.util.ElasticsearchUtils;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.BatchingQueryResult;
import org.molgenis.data.support.EntityMetaDataUtils;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.DataConverter.convert;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

/**
 * Retrieve search results in batches. Note: We do not use Elasticsearch scan & scroll, because scrolling is not
 * intended for real time user request: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current
 * /search-request-scroll.html
 */
class ElasticsearchEntityIterable extends BatchingQueryResult<Entity> implements EntityCollection
{
	private static final int BATCH_SIZE = 1000;

	private final EntityMetaData entityMeta;
	private final ElasticsearchEntityFactory elasticsearchEntityFactory;
	private final SearchRequestGenerator searchRequestGenerator;
	private final String indexName;

	private final String type;
	private final ElasticsearchUtils elasticsearchFacade;

	ElasticsearchEntityIterable(Query<Entity> q, EntityMetaData entityMetaData, ElasticsearchUtils elasticsearchFacade,
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
		Consumer<SearchRequestBuilder> searchRequestBuilderConsumer = searchRequestBuilder -> searchRequestGenerator
				.buildSearchRequest(searchRequestBuilder, type, SearchType.QUERY_AND_FETCH, q, null, null, null,
						entityMeta);
		return elasticsearchFacade.searchForIds(searchRequestBuilderConsumer, q.toString(), type, indexName)
				.map(idString -> convert(idString, entityMeta.getIdAttribute()))
				.map(idObject -> elasticsearchEntityFactory.getReference(entityMeta, idObject)).collect(toList());
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return EntityMetaDataUtils.getAttributeNames(entityMeta.getAtomicAttributes());
	}

	@Override
	public boolean isLazy()
	{
		return true;
	}
}
