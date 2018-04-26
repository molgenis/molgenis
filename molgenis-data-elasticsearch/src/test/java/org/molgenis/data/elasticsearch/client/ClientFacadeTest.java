package org.molgenis.data.elasticsearch.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableList;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchShardTarget;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.mockito.Mock;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.generator.model.*;
import org.molgenis.data.index.exception.IndexAlreadyExistsException;
import org.molgenis.data.index.exception.IndexException;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import java.util.stream.Stream;

import static ch.qos.logback.classic.Level.*;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for the exception flows of the ClientFacade.
 * We assume the other flows are sufficiently covered by the integration tests.
 */
public class ClientFacadeTest
{
	@Mock
	private Client client;

	@Mock
	private AdminClient adminClient;

	@Mock
	private IndicesAdminClient indicesAdminClient;

	@Mock
	private CreateIndexRequestBuilder createIndexRequestBuilder;

	@Mock
	private CreateIndexResponse createIndexResponse;

	@Mock
	private IndicesExistsRequestBuilder indicesExistsRequestBuilder;

	@Mock
	private DeleteIndexRequestBuilder deleteIndexRequestBuilder;

	@Mock
	private DeleteIndexResponse deleteIndexResponse;

	@Mock
	private RefreshRequestBuilder refreshRequestBuilder;

	@Mock
	private RefreshResponse refreshResponse;

	@Mock
	private SearchRequestBuilder searchRequestBuilder;

	@Mock
	private SearchResponse searchResponse;

	@Mock
	private QueryBuilder queryBuilder;

	@Mock
	private AggregationBuilder aggregationBuilder;

	@Mock
	private ExplainRequestBuilder explainRequestBuilder;

	@Mock
	private ExplainResponse explainResponse;

	@Mock
	IndexRequestBuilder indexRequestBuilder;

	@Mock
	IndexResponse indexResponse;

	@Mock
	DeleteRequestBuilder deleteRequestBuilder;

	@Mock
	DeleteResponse deleteResponse;

	@Mock
	Document document;

	@Mock
	private XContentBuilder xContentBuilder;

	@Mock
	private ShardInfo shardInfo;

	@Mock
	private Appender<ILoggingEvent> mockAppender;

	private ClientFacade clientFacade;
	private Level originalLogLevel;
	private ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
			ClientFacade.class);
	private final DefaultShardOperationFailedException[] singleShardFailure = new DefaultShardOperationFailedException[] {
			new DefaultShardOperationFailedException("index", 0, null) };

	private final org.elasticsearch.index.Index index = new org.elasticsearch.index.Index("index", "uuid");
	private final ShardSearchFailure[] singleShardSearchFailure = new ShardSearchFailure[] {
			new ShardSearchFailure("reason", new SearchShardTarget("node", index, 1)) };
	private final ShardInfo.Failure[] singleShardIndexResponseFailure = new ShardInfo.Failure[] {
			new ShardInfo.Failure(new ShardId(index, 0), "node", null, RestStatus.FAILED_DEPENDENCY, true) };

	private static ILoggingEvent matcher(Level level, String message)
	{
		return argThat(event -> message.equals(event.getFormattedMessage()) && level == event.getLevel());
	}

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		when(mockAppender.getName()).thenReturn("MOCK");
		originalLogLevel = logbackLogger.getLevel();
		logbackLogger.setLevel(Level.ALL);
		logbackLogger.addAppender(mockAppender);
	}

	@AfterClass
	public void afterClass()
	{
		logbackLogger.setLevel(originalLogLevel);
	}

	@BeforeMethod
	public void setUp() throws Exception
	{
		reset(client, adminClient, indicesAdminClient, createIndexRequestBuilder, createIndexResponse,
				indicesExistsRequestBuilder, deleteIndexRequestBuilder, deleteIndexResponse, refreshRequestBuilder,
				refreshResponse, searchRequestBuilder, searchResponse, queryBuilder, aggregationBuilder,
				explainRequestBuilder, explainResponse, indexRequestBuilder, indexResponse, shardInfo,
				deleteRequestBuilder, deleteResponse, document, xContentBuilder, shardInfo, mockAppender);
		clientFacade = new ClientFacade(client);
		when(client.admin()).thenReturn(adminClient);
		when(adminClient.indices()).thenReturn(indicesAdminClient);
	}

	@AfterMethod
	public void tearDown() throws Exception
	{
	}

	@Test(expectedExceptions = IndexAlreadyExistsException.class)
	public void testCreateIndexAlreadyExists()
	{
		Index index = Index.create("indexname");
		IndexSettings indexSettings = IndexSettings.create(1, 1);
		FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
		Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
		Stream<Mapping> mappings = Stream.of(mapping);

		when(indicesAdminClient.prepareCreate(any())).thenReturn(createIndexRequestBuilder);
		when(createIndexRequestBuilder.setSettings(any(Settings.class))).thenReturn(createIndexRequestBuilder);
		when(createIndexRequestBuilder.addMapping(any(), any(XContentBuilder.class))).thenReturn(
				createIndexRequestBuilder);

		when(createIndexRequestBuilder.get()).thenThrow(new ResourceAlreadyExistsException("Index already exists"));

		clientFacade.createIndex(index, indexSettings, mappings);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error creating index 'indexname'\\.")
	public void testCreateIndexThrowsElasticsearchException()
	{
		Index index = Index.create("indexname");
		IndexSettings indexSettings = IndexSettings.create(1, 1);
		FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
		Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
		Stream<Mapping> mappings = Stream.of(mapping);

		when(indicesAdminClient.prepareCreate("indexname")).thenReturn(createIndexRequestBuilder);
		when(createIndexRequestBuilder.setSettings(any(Settings.class))).thenReturn(createIndexRequestBuilder);
		when(createIndexRequestBuilder.addMapping(eq("type"), any(Mapping.class))).thenReturn(
				createIndexRequestBuilder);

		when(createIndexRequestBuilder.get()).thenThrow(new ElasticsearchException("error creating index"));

		clientFacade.createIndex(index, indexSettings, mappings);
	}

	@Test
	public void testCreateIndexResponseNotAcknowledgedNoExceptions()
	{
		Index index = Index.create("indexname");
		IndexSettings indexSettings = IndexSettings.create(1, 1);
		FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
		Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
		Stream<Mapping> mappings = Stream.of(mapping);

		when(indicesAdminClient.prepareCreate("indexname")).thenReturn(createIndexRequestBuilder);
		when(createIndexRequestBuilder.setSettings(any(Settings.class))).thenReturn(createIndexRequestBuilder);
		when(createIndexRequestBuilder.addMapping(eq("type"), any(Mapping.class))).thenReturn(
				createIndexRequestBuilder);

		when(createIndexRequestBuilder.get()).thenReturn(createIndexResponse);
		when(createIndexResponse.isAcknowledged()).thenReturn(false);
		when(createIndexResponse.isShardsAcked()).thenReturn(false);

		clientFacade.createIndex(index, indexSettings, mappings);

		verify(mockAppender).doAppend(matcher(TRACE, "Creating index 'indexname' ..."));
		verify(mockAppender).doAppend(matcher(WARN, "Index 'indexname' creation possibly failed (acknowledged=false)"));
		verify(mockAppender).doAppend(
				matcher(WARN, "Index 'indexname' creation possibly failed (shards_acknowledged=false)"));
		verify(mockAppender).doAppend(matcher(DEBUG, "Created index 'indexname'."));
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error determining index\\(es\\) 'index' existence\\.")
	public void testIndexesExistThrowsException()
	{
		Index index = Index.create("index");

		when(indicesAdminClient.prepareExists("index")).thenReturn(indicesExistsRequestBuilder);
		when(indicesExistsRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

		clientFacade.indexesExist(index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error deleting index\\(es\\) 'index'\\.")
	public void testDeleteIndexThrowsException()
	{
		Index index = Index.create("index");

		when(indicesAdminClient.prepareDelete("index")).thenReturn(deleteIndexRequestBuilder);
		when(deleteIndexRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

		clientFacade.deleteIndex(index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error deleting index\\(es\\) 'index'\\.")
	public void testDeleteIndexNotAcknowledged()
	{
		Index index = Index.create("index");

		when(indicesAdminClient.prepareDelete("index")).thenReturn(deleteIndexRequestBuilder);
		when(deleteIndexRequestBuilder.get()).thenReturn(deleteIndexResponse);
		when(deleteIndexResponse.isAcknowledged()).thenReturn(false);

		clientFacade.deleteIndex(index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error refreshing index\\(es\\) '_all'\\.")
	public void testRefreshIndicesThrowsException()
	{
		when(indicesAdminClient.prepareRefresh("_all")).thenReturn(refreshRequestBuilder);
		when(refreshRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

		clientFacade.refreshIndexes();
	}

	@Test(expectedExceptions = UnknownIndexException.class, expectedExceptionsMessageRegExp = "One or more indexes '_all' not found\\.")
	public void testRefreshIndicesNotFound()
	{
		when(indicesAdminClient.prepareRefresh("_all")).thenReturn(refreshRequestBuilder);
		when(refreshRequestBuilder.get()).thenThrow(new ResourceNotFoundException("exception"));

		clientFacade.refreshIndexes();
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error refreshing index\\(es\\) '_all'\\.")
	public void testRefreshIndicesFailedShards()
	{
		when(indicesAdminClient.prepareRefresh("_all")).thenReturn(refreshRequestBuilder);
		when(refreshRequestBuilder.get()).thenReturn(refreshResponse);
		when(refreshResponse.getFailedShards()).thenReturn(1);
		when(refreshResponse.getShardFailures()).thenReturn(singleShardFailure);

		clientFacade.refreshIndexes();
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error counting docs in index\\(es\\) 'index'\\.")
	public void testGetCountThrowsException()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

		clientFacade.getCount(index);
	}

	@Test(expectedExceptions = UnknownIndexException.class, expectedExceptionsMessageRegExp = "One or more indexes 'index' not found\\.")
	public void testGetCountThrowsResourceNotFoundException()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenThrow(new ResourceNotFoundException("exception"));

		clientFacade.getCount(index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error counting docs in index\\(es\\) 'index'\\.")
	public void testGetGetCountFailedShards()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenReturn(searchResponse);
		when(searchResponse.getFailedShards()).thenReturn(1);
		when(searchResponse.getShardFailures()).thenReturn(singleShardSearchFailure);

		clientFacade.getCount(index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Timeout while counting docs in index\\(es\\) 'index'\\.")
	public void testGetGetCountTimeout()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenReturn(searchResponse);
		when(searchResponse.getFailedShards()).thenReturn(0);
		when(searchResponse.isTimedOut()).thenReturn(true);

		clientFacade.getCount(index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Timeout searching counting docs in index\\(es\\) 'index'  with query 'a == b'\\.")
	public void testSearchTimedOut()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenReturn(searchResponse);
		when(searchResponse.getFailedShards()).thenReturn(0);
		when(searchResponse.isTimedOut()).thenReturn(true);
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.search(queryBuilder, 0, 100, ImmutableList.of(index));
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error searching docs in index\\(es\\) 'index' with query 'a == b'\\.")
	public void testSearchFailedShards()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenReturn(searchResponse);
		when(searchResponse.getFailedShards()).thenReturn(1);
		when(searchResponse.getShardFailures()).thenReturn(singleShardSearchFailure);
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.search(queryBuilder, 0, 100, ImmutableList.of(index));
	}

	@Test(expectedExceptions = UnknownIndexException.class, expectedExceptionsMessageRegExp = "One or more indexes 'index' not found\\.")
	public void testSearchIndexNotFound()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenThrow(new ResourceNotFoundException("Exception"));
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.search(queryBuilder, 0, 100, ImmutableList.of(index));
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error searching docs in index\\(es\\) 'index' with query 'a == b'\\.")
	public void testSearchThrowsException()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenThrow(new ElasticsearchException("Exception"));
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.search(queryBuilder, 0, 100, ImmutableList.of(index));
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error aggregating docs in index\\(es\\) 'index'\\.")
	public void testAggregateThrowsException()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenThrow(new ElasticsearchException("Exception"));
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.aggregate(ImmutableList.of(aggregationBuilder), queryBuilder, index);
	}

	@Test(expectedExceptions = UnknownIndexException.class, expectedExceptionsMessageRegExp = "One or more indexes 'index' not found\\.")
	public void testAggregateThrowsResourceNotFoundException()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenThrow(new ResourceNotFoundException("Exception"));
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.aggregate(ImmutableList.of(aggregationBuilder), queryBuilder, index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error aggregating docs in index\\(es\\) 'index'\\.")
	public void testAggregateResultHasShardFailures()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenReturn(searchResponse);
		when(searchResponse.getFailedShards()).thenReturn(1);
		when(searchResponse.getShardFailures()).thenReturn(singleShardSearchFailure);
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.aggregate(ImmutableList.of(aggregationBuilder), queryBuilder, index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Timeout aggregating docs in index\\(es\\) 'index'\\.")
	public void testAggregateTimeout()
	{
		Index index = Index.create("index");

		when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
		when(searchRequestBuilder.get()).thenReturn(searchResponse);
		when(searchResponse.getFailedShards()).thenReturn(0);
		when(searchResponse.isTimedOut()).thenReturn(true);
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.aggregate(ImmutableList.of(aggregationBuilder), queryBuilder, index);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error explaining doc with id 'id' in index 'index' for query 'a == b'\\.")
	public void testExplainThrowsException()
	{
		SearchHit searchHit = SearchHit.create("id", "index");

		when(client.prepareExplain("index", "index", "id")).thenReturn(explainRequestBuilder);
		when(explainRequestBuilder.setQuery(any())).thenReturn(explainRequestBuilder);
		when(explainRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));
		when(queryBuilder.toString()).thenReturn("a == b");

		clientFacade.explain(searchHit, queryBuilder);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error indexing doc with id 'id' in index 'index'\\.")
	public void testIndexThrowsException()
	{
		Index index = Index.create("index");

		when(document.getContent()).thenReturn(xContentBuilder);
		when(document.getId()).thenReturn("id");

		when(client.prepareIndex()).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setIndex("index")).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setType(any())).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setId("id")).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setSource(xContentBuilder)).thenReturn(indexRequestBuilder);

		when(indexRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

		clientFacade.index(index, document);
	}

	@Test(expectedExceptions = UnknownIndexException.class, expectedExceptionsMessageRegExp = "Index 'index' not found\\.")
	public void testIndexThrowsResourceNotFoundException()
	{
		Index index = Index.create("index");

		when(document.getContent()).thenReturn(xContentBuilder);
		when(document.getId()).thenReturn("id");

		when(client.prepareIndex()).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setIndex("index")).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setType(any())).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setId("id")).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setSource(xContentBuilder)).thenReturn(indexRequestBuilder);

		when(indexRequestBuilder.get()).thenThrow(new ResourceNotFoundException("exception"));

		clientFacade.index(index, document);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error indexing doc with id 'id' in index 'index'\\.")
	public void testIndexShardFailure()
	{
		Index index = Index.create("index");

		when(document.getContent()).thenReturn(xContentBuilder);
		when(document.getId()).thenReturn("id");

		when(client.prepareIndex()).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setIndex("index")).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setType(any())).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setId("id")).thenReturn(indexRequestBuilder);
		when(indexRequestBuilder.setSource(xContentBuilder)).thenReturn(indexRequestBuilder);

		when(indexRequestBuilder.get()).thenReturn(indexResponse);
		when(indexResponse.getShardInfo()).thenReturn(shardInfo);

		when(shardInfo.getSuccessful()).thenReturn(0);
		when(shardInfo.getFailures()).thenReturn(singleShardIndexResponseFailure);

		clientFacade.index(index, document);
	}

	@Test(expectedExceptions = IndexException.class, expectedExceptionsMessageRegExp = "Error deleting doc with id 'id' in index 'index'\\.")
	public void testDeleteThrowsException()
	{
		Index index = Index.create("index");

		when(document.getId()).thenReturn("id");

		when(client.prepareDelete()).thenReturn(deleteRequestBuilder);
		when(deleteRequestBuilder.setIndex("index")).thenReturn(deleteRequestBuilder);
		when(deleteRequestBuilder.setType(any())).thenReturn(deleteRequestBuilder);
		when(deleteRequestBuilder.setId("id")).thenReturn(deleteRequestBuilder);

		when(deleteRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

		clientFacade.deleteById(index, document);
	}

	@Test(expectedExceptions = UnknownIndexException.class, expectedExceptionsMessageRegExp = "Index 'index' not found\\.")
	public void testDeleteResourceNotFound()
	{
		Index index = Index.create("index");

		when(document.getId()).thenReturn("id");

		when(client.prepareDelete()).thenReturn(deleteRequestBuilder);
		when(deleteRequestBuilder.setIndex("index")).thenReturn(deleteRequestBuilder);
		when(deleteRequestBuilder.setType(any())).thenReturn(deleteRequestBuilder);
		when(deleteRequestBuilder.setId("id")).thenReturn(deleteRequestBuilder);

		when(deleteRequestBuilder.get()).thenThrow(new ResourceNotFoundException("exception"));

		clientFacade.deleteById(index, document);
	}

	@Test
	public void testCloseThrowsException() throws Exception
	{
		doThrow(new ElasticsearchException("exception")).when(client).close();

		clientFacade.close();

		verify(mockAppender).doAppend(matcher(ERROR, "Error closing Elasticsearch client"));
	}
}