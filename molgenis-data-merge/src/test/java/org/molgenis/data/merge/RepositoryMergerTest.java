package org.molgenis.data.merge;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.search.SearchHits;
import org.mockito.Mockito;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Repository;

import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

import org.elasticsearch.client.Client;

import org.springframework.stereotype.Component;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by charbonb on 01/09/14.
 */
@Component
public class RepositoryMergerTest {

    private SearchService searchService;
    private DataService dataService;

    private Client client;
    private SearchRequestBuilder searchRequestBuilder;
    private ListenableActionFuture listenableActionFuture;
    private SearchResponse searchResponse;
    private SearchHits searchHits;
    private Iterator iterator;
    private IndexRequestBuilder indexRequestBuilder;
    private DefaultEntityMetaData entityMetaData1;
    private DefaultEntityMetaData entityMetaData2;
    private DefaultEntityMetaData entityMetaDataMerged;
    private DefaultAttributeMetaData metaDataa;
    private DefaultAttributeMetaData metaDatab;
    private IndexRequestBuilder indexRequestBuilder2;

    @BeforeMethod
    public void setUp() throws Exception
    {
        entityMetaData1 = new DefaultEntityMetaData("meta1");
        entityMetaData2 = new DefaultEntityMetaData("meta2");
        entityMetaDataMerged = new DefaultEntityMetaData("mergedRepo");

        //input metadata
        metaDataa = new DefaultAttributeMetaData("a");
        metaDatab = new DefaultAttributeMetaData("b");
        DefaultAttributeMetaData metaData1c = new DefaultAttributeMetaData("c");
        DefaultAttributeMetaData metaData1d = new DefaultAttributeMetaData("d");
        DefaultAttributeMetaData metaData2c = new DefaultAttributeMetaData("c");
        DefaultAttributeMetaData metaData2e = new DefaultAttributeMetaData("e");
        entityMetaData1.addAttributeMetaData(metaDataa);
        entityMetaData1.addAttributeMetaData(metaDatab);
        entityMetaData1.addAttributeMetaData(metaData1c);
        entityMetaData1.addAttributeMetaData(metaData1d);
        entityMetaData2.addAttributeMetaData(metaDataa);
        entityMetaData2.addAttributeMetaData(metaDatab);
        entityMetaData2.addAttributeMetaData(metaData2c);
        entityMetaData2.addAttributeMetaData(metaData2e);

        //merged metadata
        DefaultAttributeMetaData idAttribute = new DefaultAttributeMetaData("ID", MolgenisFieldTypes.FieldTypeEnum.STRING);
        idAttribute.setIdAttribute(true);
        idAttribute.setVisible(false);
        entityMetaDataMerged.addAttributeMetaData(idAttribute);
        entityMetaDataMerged.setIdAttribute("ID");

        DefaultAttributeMetaData metaData1cMerged = new DefaultAttributeMetaData("meta1_c");
        DefaultAttributeMetaData metaData1dMerged = new DefaultAttributeMetaData("meta1_d");
        DefaultAttributeMetaData metaData2cMerged = new DefaultAttributeMetaData("meta2_c");
        DefaultAttributeMetaData metaData2eMerged = new DefaultAttributeMetaData("meta2_e");
        DefaultAttributeMetaData metaData1Compound = new DefaultAttributeMetaData("meta1", MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
        DefaultAttributeMetaData metaData2Compound = new DefaultAttributeMetaData("meta2", MolgenisFieldTypes.FieldTypeEnum.COMPOUND);

        List<AttributeMetaData> compound1MetaData = new ArrayList<AttributeMetaData>();
        compound1MetaData.add(metaData1cMerged);
        compound1MetaData.add(metaData1dMerged);
        metaData1Compound.setAttributesMetaData(compound1MetaData);

        List<AttributeMetaData> compound2MetaData = new ArrayList<AttributeMetaData>();
        compound2MetaData.add(metaData2cMerged);
        compound2MetaData.add(metaData2eMerged);
        metaData2Compound.setAttributesMetaData(compound2MetaData);

        entityMetaDataMerged.addAttributeMetaData(metaDataa);
        entityMetaDataMerged.addAttributeMetaData(metaDatab);
        entityMetaDataMerged.addAttributeMetaData(metaData1Compound);
        entityMetaDataMerged.addAttributeMetaData(metaData2Compound);

        searchService = mock(SearchService.class);
        client = mock(Client.class);
        searchRequestBuilder = mock(SearchRequestBuilder.class);
        listenableActionFuture = mock(ListenableActionFuture.class);
        searchResponse = mock(SearchResponse.class);
        searchHits = mock(SearchHits.class);
        iterator = mock(Iterator.class);
        indexRequestBuilder = mock(IndexRequestBuilder.class);
        indexRequestBuilder2 = mock(IndexRequestBuilder.class);

        when(client.prepareSearch("testindex")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes("mergedRepo")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes("meta1")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setTypes("meta2")).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.setSize(Integer.MAX_VALUE)).thenReturn(searchRequestBuilder);
        when(searchRequestBuilder.execute()).thenReturn(listenableActionFuture);
        when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.iterator()).thenReturn(iterator);

        when(client.prepareIndex("testindex", "meta1")).thenReturn(indexRequestBuilder);
        when(client.prepareIndex("testindex", "meta2")).thenReturn(indexRequestBuilder2);

        when(listenableActionFuture.actionGet()).thenReturn(searchResponse);
    }

    @AfterMethod
    public void teardown()
    {
        Mockito.reset(searchService);
    }

    @Test
    public void mergeTest()
    {
        ElasticsearchRepository repo1 = new ElasticsearchRepository(entityMetaData1,searchService);
        ElasticsearchRepository repo2 = new ElasticsearchRepository(entityMetaData2, searchService);

        List<Repository> repositoryList = new ArrayList<Repository>();
        repositoryList.add(repo1);
        repositoryList.add(repo2);

        List<AttributeMetaData> commonAttributes = new ArrayList<AttributeMetaData>();
        commonAttributes.add(metaDataa);
        commonAttributes.add(metaDatab);

        //do it!
        RepositoryMerger repositoryMerger = new RepositoryMerger(dataService);

        //check metaData
        assertEquals(entityMetaDataMerged.getAttributes(),repositoryMerger.mergeMetaData(repositoryList,commonAttributes,"mergedRepo").getAttributes());
    }
}
