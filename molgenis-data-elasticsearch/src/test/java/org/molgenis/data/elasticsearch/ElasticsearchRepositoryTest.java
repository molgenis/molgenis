package org.molgenis.data.elasticsearch;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.config.ElasticSearchClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class ElasticsearchRepositoryTest
{
    private ElasticsearchRepository elasticsearchRepository;
    private ElasticsearchRepositoryCollection elasticsearchRepositoryCollection;
    private DataService dataService;
    private ElasticSearchClient elasticSearchClient;
    private AdminClient adminClient;
    private IndicesAdminClient indicesAdminClient;
    private Client client;
    private EntityMetaData entityMetaData;
    private AttributeMetaData attributeMetaData;

    @BeforeMethod
    public void setUp() throws IOException
    {
        dataService = mock(DataService.class);
        elasticSearchClient = mock(ElasticSearchClient.class);
        adminClient = mock(AdminClient.class);
        indicesAdminClient = mock(IndicesAdminClient.class);
        client = mock(Client.class);
        entityMetaData = mock(EntityMetaData.class);
        when(entityMetaData.getName()).thenReturn("testRepo");

        elasticsearchRepository = new ElasticsearchRepository(client, ElasticsearchRepositoryCollection.INDEX_NAME, entityMetaData,
                dataService);
        attributeMetaData = mock(AttributeMetaData.class);
        when(entityMetaData.getAttributes()).thenReturn(Collections.singletonList(attributeMetaData));
        when(attributeMetaData.getName()).thenReturn("test");
    }

    @Test
    public void testGetEntityMetaData()
    {
        assertEquals(elasticsearchRepository.getEntityMetaData(), entityMetaData);
    }

    @Test
    public void testGetUrl()
    {
        assertEquals(elasticsearchRepository.getUrl(), ElasticsearchRepository.BASE_URL + "testRepo/");
    }

    @Test
    public void testGetName()
    {
        assertEquals(elasticsearchRepository.getName(), "testRepo");
    }

    @Test
    public void testQuery()
    {
        assertEquals(elasticsearchRepository.query(), new QueryImpl(elasticsearchRepository));
    }

    @Test
    public void testCount()
    {
        CountRequestBuilder countRequestBuilder = mock(CountRequestBuilder.class);
        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
        CountResponse countResponse = mock(CountResponse.class);
        when(client.prepareCount("molgenis")).thenReturn(countRequestBuilder);
        when(countRequestBuilder.setTypes("testRepo")).thenReturn(countRequestBuilder);
        when(countRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(countResponse);
        elasticsearchRepository.count();
        verify(countResponse).getCount();
    }

    @Test
    public void testFindAll()
    {
        SearchHit hit1 = mock(SearchHit.class);
        SearchHit hit2 = mock(SearchHit.class);
        SearchHit[] hits = new SearchHit[]{hit1, hit2};
        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        Map<String, Object> hitSource = new HashMap<String,Object>();
        hitSource.put("test","testValue");
        ElasticsearchEntity entity1 = new ElasticsearchEntity("1",hitSource,entityMetaData,dataService);
        ElasticsearchEntity entity2 = new ElasticsearchEntity("2",hitSource,entityMetaData,dataService);

        when(client.prepareSearch("molgenis")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes("testRepo")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.totalHits()).thenReturn(new Long(2));
        when(searchHits.getTotalHits()).thenReturn(new Long(2));
        when(searchHits.getHits()).thenReturn(hits);
        when(searchHits.hits()).thenReturn(hits);
        when(searchHits.iterator()).thenReturn(Arrays.asList(hits).iterator());
        when(hit1.getSource()).thenReturn(hitSource);
        when(hit1.getId()).thenReturn("1");
        when(hit2.getSource()).thenReturn(hitSource);
        when(hit2.getId()).thenReturn("2");

        Iterator<Entity> iter = elasticsearchRepository.findAll(new QueryImpl()).iterator();
        assertTrue(iter.hasNext());
        assertEquals(iter.next().getIdValue(), entity1.getIdValue());
        assertEquals(iter.next().getIdValue(), entity2.getIdValue());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testFindOne()
    {
        SearchHit hit1 = mock(SearchHit.class);
        SearchHit hit2 = mock(SearchHit.class);
        Map<String, Object> hitSource = new HashMap<String,Object>();
        hitSource.put("test","testValue");
        ElasticsearchEntity entity1 = new ElasticsearchEntity("1",hitSource,entityMetaData,dataService);
        GetRequestBuilder getRequestBuilder = mock(GetRequestBuilder.class);
        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
        GetResponse getResponse = mock(GetResponse.class);

        when(client.prepareGet("molgenis", "testRepo","1")).thenReturn(getRequestBuilder);
        when(getRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(getResponse);
        when(getResponse.isExists()).thenReturn(true);
        when(getResponse.getId()).thenReturn("1");
        when(getResponse.getSource()).thenReturn(hitSource);

        assertEquals(elasticsearchRepository.findOne("1").getIdValue(), entity1.getIdValue());
        assertEquals(elasticsearchRepository.findOne("1").get("test"), entity1.get("test"));
        assertEquals(elasticsearchRepository.findOne("1").getEntityMetaData(), entity1.getEntityMetaData());
    }

    @Test
    public void testIterator()
    {
        SearchHit hit1 = mock(SearchHit.class);
        SearchHit hit2 = mock(SearchHit.class);
        SearchHit[] hits = new SearchHit[]{hit1, hit2};
        SearchRequestBuilder searchRequestBuilder = mock(SearchRequestBuilder.class);
        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        Map<String, Object> hitSource = new HashMap<String,Object>();
        hitSource.put("test","testValue");
        ElasticsearchEntity entity1 = new ElasticsearchEntity("1",hitSource,entityMetaData,dataService);
        ElasticsearchEntity entity2 = new ElasticsearchEntity("2",hitSource,entityMetaData,dataService);

        when(client.prepareSearch("molgenis")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes("testRepo")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setSize(Integer.MAX_VALUE)).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.totalHits()).thenReturn(new Long(2));
        when(searchHits.getTotalHits()).thenReturn(new Long(2));
        when(searchHits.getHits()).thenReturn(hits);
        when(searchHits.hits()).thenReturn(hits);
        when(searchHits.iterator()).thenReturn(Arrays.asList(hits).iterator());
        when(hit1.getSource()).thenReturn(hitSource);
        when(hit1.getId()).thenReturn("1");
        when(hit2.getSource()).thenReturn(hitSource);
        when(hit2.getId()).thenReturn("2");

        Iterator<Entity> iter = elasticsearchRepository.iterator();
        assertTrue(iter.hasNext());
        assertEquals(iter.next().getIdValue(), entity1.getIdValue());
        assertEquals(iter.next().getIdValue(), entity2.getIdValue());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testAdd()
    {
        Map<String, Object> hitSource = new HashMap<String,Object>();
        hitSource.put("test","testValue");
        IndexRequestBuilder indexRequestBuilder = mock(IndexRequestBuilder.class);
        ElasticsearchEntity entity1 = new ElasticsearchEntity("1",hitSource,entityMetaData,dataService);
        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

        when(client.prepareIndex("molgenis", "testRepo")).thenReturn(indexRequestBuilder);
        when(attributeMetaData.getDataType()).thenReturn(MolgenisFieldTypes.getType(MolgenisFieldTypes.FieldTypeEnum.STRING.toString().toLowerCase()));
        when(indexRequestBuilder.setSource(hitSource)).thenReturn(indexRequestBuilder);
        when(indexRequestBuilder.execute()).thenReturn(listenableActionFuture);

        elasticsearchRepository.add(entity1);
        verify(listenableActionFuture).actionGet();
    }

    @Test
         public void testUpdate()
{
    Map<String, Object> hitSource = new HashMap<String,Object>();
    hitSource.put("test","testValue");
    UpdateRequestBuilder updateRequestBuilder = mock(UpdateRequestBuilder.class);
    ElasticsearchEntity entity1 = new ElasticsearchEntity("1",hitSource,entityMetaData,dataService);
    ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);

    when(client.prepareUpdate("molgenis", "testRepo", "1")).thenReturn(updateRequestBuilder);
    when(attributeMetaData.getDataType()).thenReturn(MolgenisFieldTypes.getType(MolgenisFieldTypes.FieldTypeEnum.STRING.toString().toLowerCase()));
    when(updateRequestBuilder.setDoc(hitSource)).thenReturn(updateRequestBuilder);
    when(updateRequestBuilder.execute()).thenReturn(listenableActionFuture);

    elasticsearchRepository.update(entity1);
    verify(listenableActionFuture).actionGet();
}
    @Test
    public void testDelete()
    {
        Map<String, Object> hitSource = new HashMap<String,Object>();
        hitSource.put("test","testValue");
        DeleteRequestBuilder deleteRequestBuilder = mock(DeleteRequestBuilder.class);
        ElasticsearchEntity entity1 = new ElasticsearchEntity("1",hitSource,entityMetaData,dataService);
        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
        DeleteResponse deleteResponse = mock(DeleteResponse.class);

        when(client.prepareDelete("molgenis", "testRepo", "1")).thenReturn(deleteRequestBuilder);
        when(attributeMetaData.getDataType()).thenReturn(MolgenisFieldTypes.getType(MolgenisFieldTypes.FieldTypeEnum.STRING.toString().toLowerCase()));
        when(deleteRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(deleteResponse);
        when(deleteResponse.isFound()).thenReturn(true);

        elasticsearchRepository.delete(entity1);
        verify(listenableActionFuture).actionGet();
    }

    @Test
    public void testDeleteAll()
    {
        Map<String, Object> hitSource = new HashMap<String,Object>();
        hitSource.put("test","testValue");
        DeleteByQueryRequestBuilder deleteRequestBuilder = mock(DeleteByQueryRequestBuilder.class);
        ListenableActionFuture listenableActionFuture = mock(ListenableActionFuture.class);
        DeleteByQueryResponse deleteByQueryResponse = mock(DeleteByQueryResponse.class);
        IndexDeleteByQueryResponse indexDeleteByQueryResponse = mock(IndexDeleteByQueryResponse.class);

        when(client.prepareDeleteByQuery("molgenis")).thenReturn(deleteRequestBuilder);
        when(deleteRequestBuilder.setTypes("testRepo")).thenReturn(deleteRequestBuilder);
        when(deleteRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(deleteByQueryResponse);
        when(deleteByQueryResponse.iterator()).thenReturn(Arrays.asList(indexDeleteByQueryResponse).iterator());

        elasticsearchRepository.deleteAll();
        verify(listenableActionFuture).actionGet();
    }
}
