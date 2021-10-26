package org.molgenis.data.elasticsearch.client;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.TRACE;
import static ch.qos.logback.classic.Level.WARN;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.OriginalIndices;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchShardTarget;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.molgenis.data.elasticsearch.client.model.SearchHit;
import org.molgenis.data.elasticsearch.generator.model.Document;
import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Index;
import org.molgenis.data.elasticsearch.generator.model.IndexSettings;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.data.elasticsearch.generator.model.MappingType;
import org.molgenis.data.index.exception.AggregationException;
import org.molgenis.data.index.exception.AggregationTimeoutException;
import org.molgenis.data.index.exception.DocumentDeleteException;
import org.molgenis.data.index.exception.DocumentIndexException;
import org.molgenis.data.index.exception.ExplainException;
import org.molgenis.data.index.exception.IndexAlreadyExistsException;
import org.molgenis.data.index.exception.IndexCountException;
import org.molgenis.data.index.exception.IndexCreateException;
import org.molgenis.data.index.exception.IndexDeleteException;
import org.molgenis.data.index.exception.IndexExistsException;
import org.molgenis.data.index.exception.IndexRefreshException;
import org.molgenis.data.index.exception.IndexSearchException;
import org.molgenis.data.index.exception.IndexSearchTimeoutException;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for the exception flows of the ClientFacade. We assume the other flows are
 * sufficiently covered by the integration tests.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ClientFacadeTest {
  @Mock private RestHighLevelClient client;

  @Mock private IndicesClient indicesClient;

  @Mock private CreateIndexResponse createIndexResponse;

  @Mock private AcknowledgedResponse deleteIndexResponse;

  @Mock private RefreshResponse refreshResponse;

  @Mock private SearchResponse searchResponse;

  @Mock private CountResponse countResponse;

  @Mock private QueryBuilder queryBuilder;

  @Mock private AggregationBuilder aggregationBuilder;

  @Mock IndexResponse indexResponse;

  @Mock Document document;

  @Mock private ShardInfo shardInfo;

  @Mock private Appender<ILoggingEvent> mockAppender;

  private ClientFacade clientFacade;
  private XContentBuilder xContentBuilder;
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
    when(mockAppender.getName()).thenReturn("MOCK");
    originalLogLevel = logbackLogger.getLevel();
    logbackLogger.setLevel(Level.ALL);
    logbackLogger.addAppender(mockAppender);

    clientFacade = new ClientFacade(client);
    when(client.indices()).thenReturn(indicesClient);
    xContentBuilder = XContentFactory.contentBuilder(XContentType.JSON);
    xContentBuilder.startObject();
    xContentBuilder.field("foo", "bar");
    xContentBuilder.endObject();
  }

  @AfterEach
  void tearDown() throws Exception {
    logbackLogger.setLevel(originalLogLevel);
  }

  @Test
  void testCreateIndexAlreadyExists() throws IOException {
    Index index = Index.create("indexname");
    IndexSettings indexSettings = IndexSettings.create(1, 1);
    FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
    Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
    Stream<Mapping> mappings = Stream.of(mapping);

    when(indicesClient.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ResourceAlreadyExistsException("Index already exists"));

    var exception =
        assertThrows(
            IndexAlreadyExistsException.class,
            () -> clientFacade.createIndex(index, indexSettings, mappings));
    assertEquals(List.of("indexname"), exception.getIndices());
  }

  @Test
  void testCreateIndexThrowsElasticsearchException() throws IOException {
    Index index = Index.create("indexname");
    IndexSettings indexSettings = IndexSettings.create(1, 1);
    FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
    Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
    Stream<Mapping> mappings = Stream.of(mapping);

    when(indicesClient.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ElasticsearchException("error creating index"));

    var exception =
        assertThrows(
            IndexCreateException.class,
            () -> clientFacade.createIndex(index, indexSettings, mappings));
    assertEquals(List.of("indexname"), exception.getIndices());
  }

  @Test
  void testCreateIndexResponseNotAcknowledgedNoExceptions() throws IOException {
    Index index = Index.create("indexname");
    IndexSettings indexSettings = IndexSettings.create(1, 1);
    FieldMapping idField = FieldMapping.create("id", MappingType.TEXT, emptyList());
    Mapping mapping = Mapping.create("type", ImmutableList.of(idField));
    Stream<Mapping> mappings = Stream.of(mapping);

    when(indicesClient.create(any(CreateIndexRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(createIndexResponse);
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
  void testIndexesExistThrowsException() throws IOException {
    Index index = Index.create("index");

    when(indicesClient.exists(any(GetIndexRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ElasticsearchException("exception"));

    var exception =
        assertThrows(IndexExistsException.class, () -> clientFacade.indexesExist(index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testDeleteIndexThrowsException() throws IOException {
    Index index = Index.create("index");

    when(indicesClient.delete(any(DeleteIndexRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ElasticsearchException("exception"));

    var exception = assertThrows(IndexDeleteException.class, () -> clientFacade.deleteIndex(index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testDeleteIndexNotAcknowledged() throws IOException {
    Index index = Index.create("index");

    when(indicesClient.delete(any(DeleteIndexRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(deleteIndexResponse);
    when(deleteIndexResponse.isAcknowledged()).thenReturn(false);

    var exception = assertThrows(IndexDeleteException.class, () -> clientFacade.deleteIndex(index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testRefreshIndicesThrowsException() throws IOException {
    when(indicesClient.refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ElasticsearchException("exception"));

    var exception = assertThrows(IndexRefreshException.class, () -> clientFacade.refreshIndexes());
    assertEquals(List.of("_all"), exception.getIndices());
  }

  @Test
  void testRefreshIndicesNotFound() throws IOException {
    when(indicesClient.refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ResourceNotFoundException("exception"));

    var exception = assertThrows(UnknownIndexException.class, () -> clientFacade.refreshIndexes());
    assertEquals(List.of("_all"), exception.getIndices());
  }

  @Test
  void testRefreshIndicesFailedShards() throws IOException {
    when(indicesClient.refresh(any(RefreshRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(refreshResponse);
    when(refreshResponse.getFailedShards()).thenReturn(1);
    when(refreshResponse.getShardFailures()).thenReturn(singleShardFailure);

    var exception = assertThrows(IndexRefreshException.class, () -> clientFacade.refreshIndexes());
    assertEquals(List.of("_all"), exception.getIndices());
  }

  @Test
  void testGetCountThrowsException() throws IOException {
    Index index = Index.create("index");
    when(client.count(any(CountRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ElasticsearchException("exception"));

    var exception = assertThrows(IndexCountException.class, () -> clientFacade.getCount(index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testGetCountThrowsNotFoundException() throws IOException {
    Index index = Index.create("index");

    when(client.count(any(CountRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ElasticsearchStatusException("exception", RestStatus.NOT_FOUND));

    var exception = assertThrows(UnknownIndexException.class, () -> clientFacade.getCount(index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testGetGetCountFailedShards() throws IOException {
    Index index = Index.create("index");

    when(client.count(any(CountRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(countResponse);
    when(countResponse.getFailedShards()).thenReturn(1);
    when(countResponse.getShardFailures()).thenReturn(singleShardSearchFailure);

    var exception = assertThrows(IndexCountException.class, () -> clientFacade.getCount(index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testGetGetCountTimeout() throws IOException {
    Index index = Index.create("index");

    when(client.count(any(CountRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(countResponse);
    when(countResponse.getFailedShards()).thenReturn(0);
    when(countResponse.isTerminatedEarly()).thenReturn(true);

    var exception = assertThrows(IndexCountException.class, () -> clientFacade.getCount(index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testSearchTimedOut() throws IOException {
    Index index = Index.create("index");

    when(client.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(0);
    when(searchResponse.isTimedOut()).thenReturn(true);
    when(searchResponse.getTook()).thenReturn(TimeValue.timeValueMillis(200));

    var indices = List.of(index);
    var exception =
        assertThrows(
            IndexSearchTimeoutException.class,
            () -> clientFacade.search(queryBuilder, 0, 100, indices));
    assertEquals(List.of("index"), exception.getIndices());
    assertEquals("", exception.getQuery());
    assertEquals(200L, exception.getMillis());
  }

  @Test
  void testSearchFailedShards() throws IOException {
    Index index = Index.create("index");

    when(client.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(1);
    when(searchResponse.getShardFailures()).thenReturn(singleShardSearchFailure);

    var indices = List.of(index);
    var exception =
        assertThrows(
            IndexSearchException.class, () -> clientFacade.search(queryBuilder, 0, 100, indices));
    assertEquals(List.of("index"), exception.getIndices());
    assertEquals("", exception.getQuery());
  }

  @Test
  void testSearchIndexNotFound() throws IOException {
    Index index = Index.create("index");

    when(client.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ResourceNotFoundException("Exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    var indices = ImmutableList.of(index);
    var exception =
        assertThrows(
            UnknownIndexException.class, () -> clientFacade.search(queryBuilder, 0, 100, indices));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testSearchThrowsException() throws IOException {
    Index index = Index.create("index");

    when(client.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ElasticsearchException("Exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    var indices = List.of(index);
    var exception =
        assertThrows(
            IndexSearchException.class, () -> clientFacade.search(queryBuilder, 0, 100, indices));
    assertEquals(List.of("index"), exception.getIndices());
    assertEquals("", exception.getQuery());
  }

  @Test
  void testAggregateThrowsException() throws IOException {
    Index index = Index.create("index");

    when(client.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ElasticsearchException("Exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    var aggregations = List.of(aggregationBuilder);
    var exception =
        assertThrows(
            AggregationException.class,
            () -> clientFacade.aggregate(aggregations, queryBuilder, index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testAggregateThrowsResourceNotFoundException() throws IOException {
    Index index = Index.create("index");

    when(client.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ResourceNotFoundException("Exception"));
    when(queryBuilder.toString()).thenReturn("a == b");

    var aggregations = ImmutableList.of(aggregationBuilder);
    var exception =
        assertThrows(
            UnknownIndexException.class,
            () -> clientFacade.aggregate(aggregations, queryBuilder, index));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testAggregateResultHasShardFailures() throws IOException {
    Index index = Index.create("index");

    when(client.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(1);
    when(searchResponse.getShardFailures()).thenReturn(singleShardSearchFailure);

    var aggregations = List.of(aggregationBuilder);
    var exception =
        assertThrows(
            AggregationException.class,
            () -> clientFacade.aggregate(aggregations, queryBuilder, index));
    assertEquals(List.of("index"), exception.getIndices());
    assertEquals(
        "indices:index, detailMessage:{\"shard\":1,\"index\":\"cluster:index\",\"node\":\"node\",\"reason\":{\"type\":\"i_o_exception\",\"reason\":\"reason\"}}",
        exception.getMessage());
  }

  @Test
  void testAggregateTimeout() throws IOException {
    Index index = Index.create("index");

    when(client.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(searchResponse);
    when(searchResponse.getFailedShards()).thenReturn(0);
    when(searchResponse.isTimedOut()).thenReturn(true);
    when(searchResponse.getTook()).thenReturn(TimeValue.timeValueMillis(200));

    var aggregations = ImmutableList.of(aggregationBuilder);
    var exception =
        assertThrows(
            AggregationTimeoutException.class,
            () -> clientFacade.aggregate(aggregations, queryBuilder, index));
    assertEquals(List.of("index"), exception.getIndices());
    assertEquals(200L, exception.getMillis());
  }

  @Test
  void testExplainThrowsException() throws IOException {
    SearchHit searchHit = SearchHit.create("id", "index");

    var cause = new ElasticsearchException("exception");
    when(client.explain(any(ExplainRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(cause);

    var exception =
        assertThrows(ExplainException.class, () -> clientFacade.explain(searchHit, queryBuilder));
    assertEquals(List.of("index"), exception.getIndices());
    assertEquals("", exception.getQuery());
    assertSame(cause, exception.getCause());
  }

  @Test
  void testIndexThrowsException() throws IOException {
    Index index = Index.create("index");

    when(document.getContent()).thenReturn(xContentBuilder);
    when(document.getId()).thenReturn("id");

    var cause = new ElasticsearchException("exception");
    when(client.index(any(IndexRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(cause);

    var exception =
        assertThrows(DocumentIndexException.class, () -> clientFacade.index(index, document));
    assertEquals(List.of("index"), exception.getIndices());
    assertSame(cause, exception.getCause());
  }

  @Test
  void testIndexThrowsResourceNotFoundException() throws IOException {
    Index index = Index.create("index");

    when(document.getContent()).thenReturn(xContentBuilder);
    when(document.getId()).thenReturn("id");

    var cause = new ResourceNotFoundException("exception");
    when(client.index(any(IndexRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(cause);

    var exception =
        assertThrows(UnknownIndexException.class, () -> clientFacade.index(index, document));
    assertEquals(List.of("index"), exception.getIndices());
    assertSame(cause, exception.getCause());
  }

  @Test
  void testIndexShardFailure() throws IOException {
    Index index = Index.create("index");

    when(document.getContent()).thenReturn(xContentBuilder);
    when(document.getId()).thenReturn("id");

    when(client.index(any(IndexRequest.class), eq(RequestOptions.DEFAULT)))
        .thenReturn(indexResponse);
    when(indexResponse.getShardInfo()).thenReturn(shardInfo);

    when(shardInfo.getSuccessful()).thenReturn(0);
    when(shardInfo.getFailures()).thenReturn(singleShardIndexResponseFailure);

    var exception =
        assertThrows(DocumentIndexException.class, () -> clientFacade.index(index, document));
    assertEquals(List.of("index"), exception.getIndices());
    assertEquals("id", exception.getDocumentId());
  }

  @Test
  void testDeleteThrowsException() throws IOException {
    Index index = Index.create("index");

    when(document.getId()).thenReturn("id");

    var cause = new ElasticsearchException("exception");
    when(client.delete(any(DeleteRequest.class), eq(RequestOptions.DEFAULT))).thenThrow(cause);

    var exception =
        assertThrows(DocumentDeleteException.class, () -> clientFacade.deleteById(index, document));
    assertEquals(List.of("index"), exception.getIndices());
    assertSame(cause, exception.getCause());
    assertEquals("id", exception.getDocumentId());
  }

  @Test
  void testDeleteResourceNotFound() throws IOException {
    Index index = Index.create("index");

    when(document.getId()).thenReturn("id");

    when(client.delete(any(DeleteRequest.class), eq(RequestOptions.DEFAULT)))
        .thenThrow(new ResourceNotFoundException("exception"));

    var exception =
        assertThrows(UnknownIndexException.class, () -> clientFacade.deleteById(index, document));
    assertEquals(List.of("index"), exception.getIndices());
  }

  @Test
  void testCloseThrowsException() throws Exception {
    doThrow(new ElasticsearchException("exception")).when(client).close();

    clientFacade.close();

    verify(mockAppender).doAppend(matcher(ERROR, "Error closing Elasticsearch client"));
  }
}
