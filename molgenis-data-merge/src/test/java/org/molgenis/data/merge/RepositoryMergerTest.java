package org.molgenis.data.merge;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.*;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Component;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by charbonb on 01/09/14.
 */
@Component
public class RepositoryMergerTest {


    private AttributeMetaData metaDataa;
    private AttributeMetaData metaDatab;
    private Repository repository1;
    private CrudRepository elasticSearchRepository;
    private DataService dataService;
    private DefaultEntityMetaData entityMetaData1;
    private SearchService searchService;
    private DefaultEntityMetaData entityMetaData2;
    private DefaultEntityMetaData entityMetaDataMerged;

    @BeforeMethod
    public void setUp() throws IOException
    {
        repository1 = mock(CrudRepository.class);
        elasticSearchRepository = mock(ElasticsearchRepository.class);

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
        entityMetaData1.setIdAttribute(metaDataa.getName());
        entityMetaData2.addAttributeMetaData(metaDataa);
        entityMetaData2.addAttributeMetaData(metaDatab);
        entityMetaData2.addAttributeMetaData(metaData2c);
        entityMetaData2.addAttributeMetaData(metaData2e);
        entityMetaData2.setIdAttribute(metaDataa.getName());

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
        dataService = mock(DataService.class);
    }

    @AfterMethod
    public void teardown()
    {
        Mockito.reset(searchService);
    }

    @Test
    public void mergeMetaDataTest()
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

    @Test
    public void mergeTest()
    {
        MapEntity newEntity1 = new MapEntity(entityMetaData1);
        newEntity1.set("a","add_a1");
        newEntity1.set("b","add_b1");
        MapEntity newEntity2 = new MapEntity(entityMetaData1);
        newEntity2.set("a","add_a2");
        newEntity2.set("b","add_b2");
        MapEntity newEntity3 = new MapEntity(entityMetaData1);
        newEntity3.set("a","add_a3");
        newEntity3.set("b","add_b3");
        MapEntity existingEntity = new MapEntity(entityMetaData2);
        existingEntity.set("a","update_a");
        existingEntity.set("b","update_b");

        Query findMergedEntityQuery = new QueryImpl();
        findMergedEntityQuery.eq("a","update_a").and();
        findMergedEntityQuery.eq("b","update_b");

        when(elasticSearchRepository.findOne(findMergedEntityQuery)).thenReturn(new MapEntity());
        List<Entity> entityList = new ArrayList<Entity>();
        entityList.add(newEntity1);
        entityList.add(newEntity1);
        entityList.add(newEntity1);

        entityList.add(existingEntity);
        entityList.add(existingEntity);
        entityList.add(existingEntity);
        entityList.add(existingEntity);
        entityList.add(existingEntity);
        when(repository1.iterator()).thenReturn(entityList.iterator());
        when(elasticSearchRepository.getName()).thenReturn("mergedRepo");
        when(dataService.getRepositoryByEntityName("mergedRepo")).thenReturn(elasticSearchRepository);


        when(repository1.getEntityMetaData()).thenReturn(entityMetaData1);
        List<Repository> repositoryList = new ArrayList<Repository>();
        repositoryList.add(repository1);
        List<AttributeMetaData> commonAttributes = new ArrayList<AttributeMetaData>();
        commonAttributes.add(metaDataa);
        commonAttributes.add(metaDatab);

        RepositoryMerger repositoryMerger = new RepositoryMerger(dataService);
        repositoryMerger.merge(repositoryList,commonAttributes,elasticSearchRepository,null,2);
        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(elasticSearchRepository, times(2)).add(argument.capture());
        assertTrue(argument.getAllValues().get(0).size() == 2);
        assertTrue(argument.getAllValues().get(1).size() == 1);
        argument = ArgumentCaptor.forClass(List.class);
        verify(elasticSearchRepository, times(3)).update(argument.capture());
        assertTrue(argument.getAllValues().get(0).size() == 2);
        assertTrue(argument.getAllValues().get(1).size() == 2);
        assertTrue(argument.getAllValues().get(2).size() == 1);
    }
}
