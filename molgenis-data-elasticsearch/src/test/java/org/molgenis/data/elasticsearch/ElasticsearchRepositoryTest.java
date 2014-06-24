package org.molgenis.data.elasticsearch;

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.collect.Lists;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.config.ElasticSearchClient;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
    }

    @Test
    public void getEntityMetaData()
    {
        assertEquals(elasticsearchRepository.getEntityMetaData(), entityMetaData);
    }

    @Test
    public void getUrl()
    {
        assertEquals(elasticsearchRepository.getUrl(), ElasticsearchRepository.BASE_URL + "testRepo/");
    }

    @Test
    public void getName()
    {
        assertEquals(elasticsearchRepository.getName(), "testRepo");
    }

    @Test
    public void query()
    {
        assertEquals(elasticsearchRepository.query(), new QueryImpl(elasticsearchRepository));
    }
}
