package org.molgenis.data.elasticsearch.client;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.TRACE;
import static ch.qos.logback.classic.Level.WARN;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.stream.Stream;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.OriginalIndices;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
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
import org.elasticsearch.action.support.master.AcknowledgedResponse;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.generator.model.Document;
import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Index;
import org.molgenis.data.elasticsearch.generator.model.IndexSettings;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.generator.model.MappingType;
import org.molgenis.data.index.exception.IndexAlreadyExistsException;
import org.molgenis.data.index.exception.IndexException;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for the exception flows of the ClientFacade. We assume the other flows are
 * sufficiently covered by the integration tests.
 */
class ClientFacadeTest {
  @Mock private Client client;

  @Mock private AdminClient adminClient;

  @Mock private IndicesAdminClient indicesAdminClient;

  @Mock private CreateIndexRequestBuilder createIndexRequestBuilder;

  @Mock private CreateIndexResponse createIndexResponse;

  @Mock private IndicesExistsRequestBuilder indicesExistsRequestBuilder;

  @Mock private DeleteIndexRequestBuilder deleteIndexRequestBuilder;

  @Mock private AcknowledgedResponse deleteIndexResponse;

  @Mock private RefreshRequestBuilder refreshRequestBuilder;

  @Mock private RefreshResponse refreshResponse;

  @Mock private SearchRequestBuilder searchRequestBuilder;

  @Mock private SearchResponse searchResponse;

  @Mock private QueryBuilder queryBuilder;

  @Mock private AggregationBuilder aggregationBuilder;

  @Mock private ExplainRequestBuilder explainRequestBuilder;

  @Mock private ExplainResponse explainResponse;

  @Mock IndexRequestBuilder indexRequestBuilder;

  @Mock IndexResponse indexResponse;

  @Mock DeleteRequestBuilder deleteRequestBuilder;

  @Mock DeleteResponse deleteResponse;

  @Mock Document document;

  @Mock private XContentBuilder xContentBuilder;

  @Mock private ShardInfo shardInfo;

  @Mock private Appender<ILoggingEvent> mockAppender;

  private ClientFacade clientFacade;
  private Level originalLogLevel;
  private ch.qos.logback.classic.Logger logbackLogger =
      (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ClientFacade.class);
  private final DefaultShardOperationFailedException[] singleShardFailure =
      new DefaultShardOperationFailedException[] {
        new DefaultShardOperationFailedException("index", 0, new Exception())
      };

  private final org.elasticsearch.index.Index index =
      new org.elasticsearch.index.Index("index", "uuid");
  private final ShardSearchFailure[] singleShardSearchFailure =
      new ShardSearchFailure[] {
        new ShardSearchFailure(
            new IOException("reason"),
            new SearchShardTarget("node", new ShardId(index, 1), "cluster", OriginalIndices.NONE))
      };
  private final ShardInfo.Failure[] singleShardIndexResponseFailure =
      new ShardInfo.Failure[] {
        new ShardInfo.Failure(
            new ShardId(index, 0), "node", new Exception(), RestStatus.FAILED_DEPENDENCY, true)
      };

  private static ILoggingEvent matcher(Level level, String message) {
    return argThat(
        event -> message.equals(event.getFormattedMessage()) && level == event.getLevel());
  }

  @BeforeEach
  void setUp() throws Exception {
    initMocks(this);
    when(mockAppender.getName()).thenReturn("MOCK");
    originalLogLevel = logbackLogger.getLevel();
    logbackLogger.setLevel(Level.ALL);
    logbackLogger.addAppender(mockAppender);

    reset(
        client,
        adminClient,
        indicesAdminClient,
        createIndexRequestBuilder,
        createIndexResponse,
        indicesExistsRequestBuilder,
        deleteIndexRequestBuilder,
        deleteIndexResponse,
        refreshRequestBuilder,
        refreshResponse,
        searchRequestBuilder,
        searchResponse,
        queryBuilder,
        aggregationBuilder,
        explainRequestBuilder,
        explainResponse,
        indexRequestBuilder,
        indexResponse,
        shardInfo,
        deleteRequestBuilder,
        deleteResponse,
        document,
        xContentBuilder,
        shardInfo,
        mockAppender);
    clientFacade = new ClientFacade(client);
    when(client.admin()).thenReturn(adminClient);
    when(adminClient.indices()).thenReturn(indicesAdminClient);
  }

  @AfterEach
  void tearDown() throws Exception {
    logbackLogger.setLevel(originalLogLevel);
  }

  @Test
  void testCreateIndexAlreadyExists() {
    Index index = Index.create("indexname");
    IndexSettings indexSettings = IndexSettings.create(1, 1);
    FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
    Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
    Stream<Mapping> mappings = Stream.of(mapping);

    when(indicesAdminClient.prepareCreate(any())).thenReturn(createIndexRequestBuilder);
    when(createIndexRequestBuilder.setSettings(any(Settings.class)))
        .thenReturn(createIndexRequestBuilder);
    when(createIndexRequestBuilder.addMapping(any(), any(XContentBuilder.class)))
        .thenReturn(createIndexRequestBuilder);

    when(createIndexRequestBuilder.get())
        .thenThrow(new ResourceAlreadyExistsException("Index already exists"));

    assertThrows(
        IndexAlreadyExistsException.class,
        () -> clientFacade.createIndex(index, indexSettings, mappings));
  }

  @Test
  void testCreateIndexThrowsElasticsearchException() {
    Index index = Index.create("indexname");
    IndexSettings indexSettings = IndexSettings.create(1, 1);
    FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
    Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
    Stream<Mapping> mappings = Stream.of(mapping);

    when(indicesAdminClient.prepareCreate("indexname")).thenReturn(createIndexRequestBuilder);
    when(createIndexRequestBuilder.setSettings(any(Settings.class)))
        .thenReturn(createIndexRequestBuilder);
    when(createIndexRequestBuilder.addMapping(eq("type"), any(Mapping.class)))
        .thenReturn(createIndexRequestBuilder);

    when(createIndexRequestBuilder.get())
        .thenThrow(new ElasticsearchException("error creating index"));

    Exception exception =
        assertThrows(
            IndexException.class, () -> clientFacade.createIndex(index, indexSettings, mappings));
    assertThat(exception.getMessage()).containsPattern("Error creating index 'indexname'\\.");
  }

  @Test
  void testCreateIndexResponseNotAcknowledgedNoExceptions() {
    Index index = Index.create("indexname");
    IndexSettings indexSettings = IndexSettings.create(1, 1);
    FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
    Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
    Stream<Mapping> mappings = Stream.of(mapping);

    when(indicesAdminClient.prepareCreate("indexname")).thenReturn(createIndexRequestBuilder);
    when(createIndexRequestBuilder.setSettings(any(Settings.class)))
        .thenReturn(createIndexRequestBuilder);
    when(createIndexRequestBuilder.addMapping(eq("type"), any(Mapping.class)))
        .thenReturn(createIndexRequestBuilder);

    when(createIndexRequestBuilder.get()).thenReturn(createIndexResponse);
    when(createIndexResponse.isAcknowledged()).thenReturn(false);
    when(createIndexResponse.isShardsAcknowledged()).thenReturn(false);

    clientFacade.createIndex(index, indexSettings, mappings);

    verify(mockAppender).doAppend(matcher(TRACE, "Creating index 'indexname' ..."));
    verify(mockAppender)
        .doAppend(matcher(WARN, "Index 'indexname' creation possibly failed (acknowledged=false)"));
    verify(mockAppender)
        .doAppend(
            matcher(
                WARN, "Index 'indexname' creation possibly failed (shards_acknowledged=false)"));
    verify(mockAppender).doAppend(matcher(DEBUG, "Created index 'indexname'."));
  }

  @Test
  void testIndexesExistThrowsException() {
    Index index = Index.create("index");

    when(indicesAdminClient.prepareExists("index")).thenReturn(indicesExistsRequestBuilder);
    when(indicesExistsRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

    Exception exception =
        assertThrows(IndexException.class, () -> clientFacade.indexesExist(index));
    assertThat(exception.getMessage())
        .containsPattern("Error determining index\\(es\\) 'index' existence\\.");
  }

  @Test
  void testDeleteIndexThrowsException() {
    Index index = Index.create("index");

    when(indicesAdminClient.prepareDelete("index")).thenReturn(deleteIndexRequestBuilder);
    when(deleteIndexRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

    Exception exception = assertThrows(IndexException.class, () -> clientFacade.deleteIndex(index));
    assertThat(exception.getMessage()).containsPattern("Error deleting index\\(es\\) 'index'\\.");
  }

  @Test
  void testDeleteIndexNotAcknowledged() {
    Index index = Index.create("index");

    when(indicesAdminClient.prepareDelete("index")).thenReturn(deleteIndexRequestBuilder);
    when(deleteIndexRequestBuilder.get()).thenReturn(deleteIndexResponse);
    when(deleteIndexResponse.isAcknowledged()).thenReturn(false);

    Exception exception = assertThrows(IndexException.class, () -> clientFacade.deleteIndex(index));
    assertThat(exception.getMessage()).containsPattern("Error deleting index\\(es\\) 'index'\\.");
  }

  @Test
  void testRefreshIndicesThrowsException() {
    when(indicesAdminClient.prepareRefresh("_all")).thenReturn(refreshRequestBuilder);
    when(refreshRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

    Exception exception = assertThrows(IndexException.class, () -> clientFacade.refreshIndexes());
    assertThat(exception.getMessage()).containsPattern("Error refreshing index\\(es\\) '_all'\\.");
  }

  @Test
  void testRefreshIndicesNotFound() {
    when(indicesAdminClient.prepareRefresh("_all")).thenReturn(refreshRequestBuilder);
    when(refreshRequestBuilder.get()).thenThrow(new ResourceNotFoundException("exception"));

    Exception exception =
        assertThrows(UnknownIndexException.class, () -> clientFacade.refreshIndexes());
    assertThat(exception.getMessage()).containsPattern("One or more indexes '_all' not found\\.");
  }

  @Test
  void testRefreshIndicesFailedShards() {
    when(indicesAdminClient.prepareRefresh("_all")).thenReturn(refreshRequestBuilder);
    when(refreshRequestBuilder.get()).thenReturn(refreshResponse);
    when(refreshResponse.getFailedShards()).thenReturn(1);
    when(refreshResponse.getShardFailures()).thenReturn(singleShardFailure);

    Exception exception = assertThrows(IndexException.class, () -> clientFacade.refreshIndexes());
    assertThat(exception.getMessage()).containsPattern("Error refreshing index\\(es\\) '_all'\\.");
  }

  @Test
  void testGetCountThrowsException() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

    Exception exception = assertThrows(IndexException.class, () -> clientFacade.getCount(index));
    assertThat(exception.getMessage())
        .containsPattern("Error counting docs in index\\(es\\) 'index'\\.");
  }

  @Test
  void testGetCountThrowsResourceNotFoundException() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenThrow(new ResourceNotFoundException("exception"));

    Exception exception =
        assertThrows(UnknownIndexException.class, () -> clientFacade.getCount(index));
    assertThat(exception.getMessage()).containsPattern("One or more indexes 'index' not found\\.");
  }

  @Test
  void testGetGetCountFailedShards() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(1);
    when(searchResponse.getShardFailures()).thenReturn(singleShardSearchFailure);

    Exception exception = assertThrows(IndexException.class, () -> clientFacade.getCount(index));
    assertThat(exception.getMessage())
        .containsPattern("Error counting docs in index\\(es\\) 'index'\\.");
  }

  @Test
  void testGetGetCountTimeout() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(0);
    when(searchResponse.isTimedOut()).thenReturn(true);

    Exception exception = assertThrows(IndexException.class, () -> clientFacade.getCount(index));
    assertThat(exception.getMessage())
        .containsPattern("Timeout while counting docs in index\\(es\\) 'index'\\.");
  }

  @Test
  void testSearchTimedOut() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(0);
    when(searchResponse.isTimedOut()).thenReturn(true);
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(
            IndexException.class,
            () -> clientFacade.search(queryBuilder, 0, 100, ImmutableList.of(index)));
    assertThat(exception.getMessage())
        .containsPattern(
            "Timeout searching counting docs in index\\(es\\) 'index'  with query 'a == b'\\.");
  }

  @Test
  void testSearchFailedShards() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(1);
    when(searchResponse.getShardFailures()).thenReturn(singleShardSearchFailure);
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(
            IndexException.class,
            () -> clientFacade.search(queryBuilder, 0, 100, ImmutableList.of(index)));
    assertThat(exception.getMessage())
        .containsPattern("Error searching docs in index\\(es\\) 'index' with query 'a == b'\\.");
  }

  @Test
  void testSearchIndexNotFound() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenThrow(new ResourceNotFoundException("Exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(
            UnknownIndexException.class,
            () -> clientFacade.search(queryBuilder, 0, 100, ImmutableList.of(index)));
    assertThat(exception.getMessage()).containsPattern("One or more indexes 'index' not found\\.");
  }

  @Test
  void testSearchThrowsException() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenThrow(new ElasticsearchException("Exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(
            IndexException.class,
            () -> clientFacade.search(queryBuilder, 0, 100, ImmutableList.of(index)));
    assertThat(exception.getMessage())
        .containsPattern("Error searching docs in index\\(es\\) 'index' with query 'a == b'\\.");
  }

  @Test
  void testAggregateThrowsException() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenThrow(new ElasticsearchException("Exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(
            IndexException.class,
            () ->
                clientFacade.aggregate(ImmutableList.of(aggregationBuilder), queryBuilder, index));
    assertThat(exception.getMessage())
        .containsPattern("Error aggregating docs in index\\(es\\) 'index'\\.");
  }

  @Test
  void testAggregateThrowsResourceNotFoundException() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenThrow(new ResourceNotFoundException("Exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(
            UnknownIndexException.class,
            () ->
                clientFacade.aggregate(ImmutableList.of(aggregationBuilder), queryBuilder, index));
    assertThat(exception.getMessage()).containsPattern("One or more indexes 'index' not found\\.");
  }

  @Test
  void testAggregateResultHasShardFailures() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(1);
    when(searchResponse.getShardFailures()).thenReturn(singleShardSearchFailure);
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(
            IndexException.class,
            () ->
                clientFacade.aggregate(ImmutableList.of(aggregationBuilder), queryBuilder, index));
    assertThat(exception.getMessage())
        .containsPattern("Error aggregating docs in index\\(es\\) 'index'\\.");
  }

  @Test
  void testAggregateTimeout() {
    Index index = Index.create("index");

    when(client.prepareSearch("index")).thenReturn(searchRequestBuilder);
    when(searchRequestBuilder.get()).thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(0);
    when(searchResponse.isTimedOut()).thenReturn(true);
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(
            IndexException.class,
            () ->
                clientFacade.aggregate(ImmutableList.of(aggregationBuilder), queryBuilder, index));
    assertThat(exception.getMessage())
        .containsPattern("Timeout aggregating docs in index\\(es\\) 'index'\\.");
  }

  @Test
  void testExplainThrowsException() {
    SearchHit searchHit = SearchHit.create("id", "index");

    when(client.prepareExplain("index", "index", "id")).thenReturn(explainRequestBuilder);
    when(explainRequestBuilder.setQuery(any())).thenReturn(explainRequestBuilder);
    when(explainRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    Exception exception =
        assertThrows(IndexException.class, () -> clientFacade.explain(searchHit, queryBuilder));
    assertThat(exception.getMessage())
        .containsPattern(
            "Error explaining doc with id 'id' in index 'index' for query 'a == b'\\.");
  }

  @Test
  void testIndexThrowsException() {
    Index index = Index.create("index");

    when(document.getContent()).thenReturn(xContentBuilder);
    when(document.getId()).thenReturn("id");

    when(client.prepareIndex()).thenReturn(indexRequestBuilder);
    when(indexRequestBuilder.setIndex("index")).thenReturn(indexRequestBuilder);
    when(indexRequestBuilder.setType(any())).thenReturn(indexRequestBuilder);
    when(indexRequestBuilder.setId("id")).thenReturn(indexRequestBuilder);
    when(indexRequestBuilder.setSource(xContentBuilder)).thenReturn(indexRequestBuilder);

    when(indexRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

    Exception exception =
        assertThrows(IndexException.class, () -> clientFacade.index(index, document));
    assertThat(exception.getMessage())
        .containsPattern("Error indexing doc with id 'id' in index 'index'\\.");
  }

  @Test
  void testIndexThrowsResourceNotFoundException() {
    Index index = Index.create("index");

    when(document.getContent()).thenReturn(xContentBuilder);
    when(document.getId()).thenReturn("id");

    when(client.prepareIndex()).thenReturn(indexRequestBuilder);
    when(indexRequestBuilder.setIndex("index")).thenReturn(indexRequestBuilder);
    when(indexRequestBuilder.setType(any())).thenReturn(indexRequestBuilder);
    when(indexRequestBuilder.setId("id")).thenReturn(indexRequestBuilder);
    when(indexRequestBuilder.setSource(xContentBuilder)).thenReturn(indexRequestBuilder);

    when(indexRequestBuilder.get()).thenThrow(new ResourceNotFoundException("exception"));

    Exception exception =
        assertThrows(UnknownIndexException.class, () -> clientFacade.index(index, document));
    assertThat(exception.getMessage()).containsPattern("Index 'index' not found\\.");
  }

  @Test
  void testIndexShardFailure() {
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

    Exception exception =
        assertThrows(IndexException.class, () -> clientFacade.index(index, document));
    assertThat(exception.getMessage())
        .containsPattern("Error indexing doc with id 'id' in index 'index'\\.");
  }

  @Test
  void testDeleteThrowsException() {
    Index index = Index.create("index");

    when(document.getId()).thenReturn("id");

    when(client.prepareDelete()).thenReturn(deleteRequestBuilder);
    when(deleteRequestBuilder.setIndex("index")).thenReturn(deleteRequestBuilder);
    when(deleteRequestBuilder.setType(any())).thenReturn(deleteRequestBuilder);
    when(deleteRequestBuilder.setId("id")).thenReturn(deleteRequestBuilder);

    when(deleteRequestBuilder.get()).thenThrow(new ElasticsearchException("exception"));

    Exception exception =
        assertThrows(IndexException.class, () -> clientFacade.deleteById(index, document));
    assertThat(exception.getMessage())
        .containsPattern("Error deleting doc with id 'id' in index 'index'\\.");
  }

  @Test
  void testDeleteResourceNotFound() {
    Index index = Index.create("index");

    when(document.getId()).thenReturn("id");

    when(client.prepareDelete()).thenReturn(deleteRequestBuilder);
    when(deleteRequestBuilder.setIndex("index")).thenReturn(deleteRequestBuilder);
    when(deleteRequestBuilder.setType(any())).thenReturn(deleteRequestBuilder);
    when(deleteRequestBuilder.setId("id")).thenReturn(deleteRequestBuilder);

    when(deleteRequestBuilder.get()).thenThrow(new ResourceNotFoundException("exception"));

    Exception exception =
        assertThrows(UnknownIndexException.class, () -> clientFacade.deleteById(index, document));
    assertThat(exception.getMessage()).containsPattern("Index 'index' not found\\.");
  }

  @Test
  void testCloseThrowsException() throws Exception {
    doThrow(new ElasticsearchException("exception")).when(client).close();

    clientFacade.close();

    verify(mockAppender).doAppend(matcher(ERROR, "Error closing Elasticsearch client"));
  }
}
