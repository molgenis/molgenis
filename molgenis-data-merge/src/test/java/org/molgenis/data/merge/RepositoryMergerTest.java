package org.molgenis.data.merge;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Component;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/**
 * Created by charbonb on 01/09/14.
 */
@Component
public class RepositoryMergerTest
{
	private AttributeMetaData idAttribute;
	private AttributeMetaData metaDataa;
	private AttributeMetaData metaDatab;
	private Repository<Entity> repository1;
	private Repository<Entity> elasticSearchRepository;
	private DataService dataService;
	private EntityMetaData entityMetaData1;
	private SearchService searchService;
	private EntityMetaData entityMetaData2;
	private EntityMetaData entityMetaDataMerged;

	@BeforeMethod
	public void setUp() throws IOException
	{
		repository1 = mock(Repository.class);
		elasticSearchRepository = mock(ElasticsearchRepository.class);

		entityMetaData1 = new EntityMetaDataImpl("meta1");
		entityMetaData2 = new EntityMetaDataImpl("meta2");
		entityMetaDataMerged = new EntityMetaDataImpl("mergedRepo");

		// input metadata
		metaDataa = new AttributeMetaData("a");
		metaDatab = new AttributeMetaData("b");
		AttributeMetaData metaData1c = new AttributeMetaData("c");
		AttributeMetaData metaData1d = new AttributeMetaData("d");
		AttributeMetaData metaData2c = new AttributeMetaData("c");
		AttributeMetaData metaData2e = new AttributeMetaData("e");
		entityMetaData1.addAttribute(metaDataa, ROLE_ID);
		entityMetaData1.addAttribute(metaDatab);
		entityMetaData1.addAttribute(metaData1c);
		entityMetaData1.addAttribute(metaData1d);
		entityMetaData2.addAttribute(metaDataa, ROLE_ID);
		entityMetaData2.addAttribute(metaDatab);
		entityMetaData2.addAttribute(metaData2c);
		entityMetaData2.addAttribute(metaData2e);

		// merged metadata
		idAttribute = new AttributeMetaData("ID", STRING);
		idAttribute.setNillable(false);
		idAttribute.setVisible(false);
		entityMetaDataMerged.addAttribute(idAttribute, ROLE_ID);

		AttributeMetaData metaData1cMerged = new AttributeMetaData("meta1_c");
		AttributeMetaData metaData1dMerged = new AttributeMetaData("meta1_d");
		AttributeMetaData metaData2cMerged = new AttributeMetaData("meta2_c");
		AttributeMetaData metaData2eMerged = new AttributeMetaData("meta2_e");
		AttributeMetaData metaData1Compound = new AttributeMetaData("meta1", COMPOUND);
		AttributeMetaData metaData2Compound = new AttributeMetaData("meta2", COMPOUND);

		List<AttributeMetaData> compound1MetaData = new ArrayList<AttributeMetaData>();
		compound1MetaData.add(metaData1cMerged);
		compound1MetaData.add(metaData1dMerged);
		metaData1Compound.setAttributeParts(compound1MetaData);

		List<AttributeMetaData> compound2MetaData = new ArrayList<AttributeMetaData>();
		compound2MetaData.add(metaData2cMerged);
		compound2MetaData.add(metaData2eMerged);
		metaData2Compound.setAttributeParts(compound2MetaData);

		entityMetaDataMerged.addAttribute(metaDataa);
		entityMetaDataMerged.addAttribute(metaDatab);
		entityMetaDataMerged.addAttribute(metaData1Compound);
		entityMetaDataMerged.addAttribute(metaData2Compound);

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
		ElasticsearchRepository repo1 = new ElasticsearchRepository(entityMetaData1, searchService);
		ElasticsearchRepository repo2 = new ElasticsearchRepository(entityMetaData2, searchService);

		List<Repository<Entity>> repositoryList = new ArrayList<Repository<Entity>>();
		repositoryList.add(repo1);
		repositoryList.add(repo2);

		List<AttributeMetaData> commonAttributes = new ArrayList<AttributeMetaData>();
		commonAttributes.add(metaDataa);
		commonAttributes.add(metaDatab);

		// do it!
		RepositoryMerger repositoryMerger = new RepositoryMerger(dataService);

		// check metaData
		assertEquals(
				Lists.newArrayList(repositoryMerger
						.mergeMetaData(repositoryList, commonAttributes, idAttribute, "mergedRepo").getAttributes()),
				Lists.newArrayList(entityMetaDataMerged.getAttributes()));
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void mergeTest()
	{
		MapEntity newEntity1 = new MapEntity(entityMetaData1);
		newEntity1.set("a", "add_a1");
		newEntity1.set("b", "add_b1");
		MapEntity newEntity2 = new MapEntity(entityMetaData1);
		newEntity2.set("a", "add_a2");
		newEntity2.set("b", "add_b2");
		MapEntity newEntity3 = new MapEntity(entityMetaData1);
		newEntity3.set("a", "add_a3");
		newEntity3.set("b", "add_b3");
		MapEntity existingEntity = new MapEntity(entityMetaData2);
		existingEntity.set("a", "update_a");
		existingEntity.set("b", "update_b");

		Query<Entity> findMergedEntityQuery = new QueryImpl<Entity>();
		findMergedEntityQuery.eq("a", "update_a").and();
		findMergedEntityQuery.eq("b", "update_b");

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
		when(dataService.getRepository("mergedRepo")).thenReturn(elasticSearchRepository);

		when(repository1.getEntityMetaData()).thenReturn(entityMetaData1);
		List<Repository<Entity>> repositoryList = new ArrayList<Repository<Entity>>();
		repositoryList.add(repository1);
		List<AttributeMetaData> commonAttributes = new ArrayList<AttributeMetaData>();
		commonAttributes.add(metaDataa);
		commonAttributes.add(metaDatab);

		RepositoryMerger repositoryMerger = new RepositoryMerger(dataService);
		repositoryMerger.merge(repositoryList, commonAttributes, elasticSearchRepository, 2);
		ArgumentCaptor<Stream<Entity>> argument = ArgumentCaptor.forClass((Class) Stream.class);
		verify(elasticSearchRepository, times(2)).add(argument.capture());
		List<Entity> list0 = argument.getAllValues().get(0).collect(toList());
		List<Entity> list1 = argument.getAllValues().get(1).collect(toList());
		assertEquals(list0.size(), 2);
		assertEquals(list1.size(), 1);
		ArgumentCaptor<Stream<Entity>> esArgument = ArgumentCaptor.forClass((Class) Stream.class);
		verify(elasticSearchRepository, times(3)).update(esArgument.capture());
		List<Entity> esList0 = esArgument.getAllValues().get(0).collect(toList());
		List<Entity> esList1 = esArgument.getAllValues().get(1).collect(toList());
		List<Entity> esList2 = esArgument.getAllValues().get(2).collect(toList());
		assertEquals(esList0.size(), 2);
		assertEquals(esList1.size(), 2);
		assertEquals(esList2.size(), 1);
	}
}
