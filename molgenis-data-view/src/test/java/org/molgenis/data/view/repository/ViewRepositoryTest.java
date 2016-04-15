package org.molgenis.data.view.repository;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
//import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.assertFalse;
//import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData.AttributeRole;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.JoinedAttributeMetaData;
import org.molgenis.data.view.meta.SlaveEntityMetaData;
import org.molgenis.data.view.meta.ViewMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ViewRepositoryTest.Config.class)
public class ViewRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;
	private ViewRepository viewRepository;
	private DefaultEntityMetaData expectedEntityMetaData;

	private EditableEntityMetaData entityMetaA;
	private EditableEntityMetaData entityMetaB;
	private EditableEntityMetaData entityMetaC;

	private Repository repoA;
	private Repository repoB;
	private Repository repoC;
	private Repository viewMetaDataRepo;

	private Entity joinedAttributeEntity1;
	private Entity joinedAttributeEntity2;

	private Entity joinedEntityEntity1;
	private Entity joinedEntityEntity2;

	private Entity viewMetaDataEntity;
	private Entity viewMetaDataEntity2;
	private Entity viewMetaDataEntity3;
	private Entity viewMetaDataEntity4;

	@BeforeClass
	public void setupBeforeClass()
	{
		// entity A (master entity)
		entityMetaA = new DefaultEntityMetaData("entityA");
		entityMetaA.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaA.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaA.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaA.addAttribute("A").setDataType(STRING);
		repoA = new InMemoryRepository(entityMetaA);

		// entity B (slave entity)
		entityMetaB = new DefaultEntityMetaData("entityB");
		entityMetaB.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaB.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaB.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaB.addAttribute("B").setDataType(STRING);
		repoB = new InMemoryRepository(entityMetaB);

		// entity C (slave entity)
		entityMetaC = new DefaultEntityMetaData("entityC");
		entityMetaC.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaC.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaC.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaC.addAttribute("C").setDataType(STRING);
		repoC = new InMemoryRepository(entityMetaC);

		ViewMetaData viewMetaData = new ViewMetaData();
		SlaveEntityMetaData joinedEntityMetaData = new SlaveEntityMetaData();
		JoinedAttributeMetaData joinedAttributeMetaData = new JoinedAttributeMetaData();

		// make repositories
		viewMetaDataRepo = new InMemoryRepository(viewMetaData);

		joinedAttributeEntity1 = new MapEntity(joinedAttributeMetaData);
		joinedAttributeEntity1.set(JoinedAttributeMetaData.IDENTIFIER, "1");
		joinedAttributeEntity1.set(JoinedAttributeMetaData.MASTER_ATTRIBUTE, "chrom");
		joinedAttributeEntity1.set(JoinedAttributeMetaData.JOIN_ATTRIBUTE, "chrom");

		joinedAttributeEntity2 = new MapEntity(joinedAttributeMetaData);
		joinedAttributeEntity2.set(JoinedAttributeMetaData.IDENTIFIER, "2");
		joinedAttributeEntity2.set(JoinedAttributeMetaData.MASTER_ATTRIBUTE, "pos");
		joinedAttributeEntity2.set(JoinedAttributeMetaData.JOIN_ATTRIBUTE, "pos");

		joinedEntityEntity1 = new MapEntity(joinedEntityMetaData);
		joinedEntityEntity1.set(SlaveEntityMetaData.IDENTIFIER, "1");
		joinedEntityEntity1.set(SlaveEntityMetaData.SLAVE_ENTITY, "entityB");
		joinedEntityEntity1.set(SlaveEntityMetaData.JOINED_ATTRIBUTES,
				newArrayList(joinedAttributeEntity1, joinedAttributeEntity2));

		joinedEntityEntity2 = new MapEntity(joinedEntityMetaData);
		joinedEntityEntity2.set(SlaveEntityMetaData.IDENTIFIER, "2");
		joinedEntityEntity2.set(SlaveEntityMetaData.SLAVE_ENTITY, "entityC");
		joinedEntityEntity2.set(SlaveEntityMetaData.JOINED_ATTRIBUTES,
				newArrayList(joinedAttributeEntity1, joinedAttributeEntity2));

		viewMetaDataEntity = new MapEntity(viewMetaData);
		viewMetaDataEntity.set(ViewMetaData.IDENTIFIER, "1");
		viewMetaDataEntity.set(ViewMetaData.NAME, "MY_FIRST_VIEW");
		viewMetaDataEntity.set(ViewMetaData.MASTER_ENTITY, "entityA");
		viewMetaDataEntity.set(ViewMetaData.SLAVE_ENTITIES, newArrayList(joinedEntityEntity1, joinedEntityEntity2));
		viewMetaDataRepo.add(viewMetaDataEntity);

		// entity A (master entity)
		DefaultEntityMetaData expectedEntityMetaA = new DefaultEntityMetaData("entityA");
		expectedEntityMetaA.addAttribute("id", ROLE_ID).setDataType(STRING);
		expectedEntityMetaA.addAttribute("chrom").setDataType(STRING).setNillable(false);
		expectedEntityMetaA.addAttribute("pos").setDataType(STRING).setNillable(false);
		expectedEntityMetaA.addAttribute("A").setDataType(STRING);

		// entity B (slave entity)
		DefaultEntityMetaData expectedEntityMetaB = new DefaultEntityMetaData("entityB");
		expectedEntityMetaB.addAttribute("entityB_id", ROLE_ID).setDataType(STRING);
		expectedEntityMetaB.addAttribute("entityB_chrom").setDataType(STRING).setNillable(false);
		expectedEntityMetaB.addAttribute("entityB_pos").setDataType(STRING).setNillable(false);
		expectedEntityMetaB.addAttribute("entityB_B").setDataType(STRING);

		// entity C (slave entity)
		DefaultEntityMetaData expectedEntityMetaC = new DefaultEntityMetaData("entityC");
		expectedEntityMetaC.addAttribute("entityC_id", ROLE_ID).setDataType(STRING);
		expectedEntityMetaC.addAttribute("entityC_chrom").setDataType(STRING).setNillable(false);
		expectedEntityMetaC.addAttribute("entityC_pos").setDataType(STRING).setNillable(false);
		expectedEntityMetaC.addAttribute("entityC_C").setDataType(STRING);

		// Create the expected metadata
		expectedEntityMetaData = new DefaultEntityMetaData("MY_FIRST_VIEW", PackageImpl.defaultPackage);
		expectedEntityMetaData.addAttribute("id", AttributeRole.ROLE_ID, AttributeRole.ROLE_LABEL);
		expectedEntityMetaData.setAbstract(false);
		DefaultAttributeMetaData entityACompound = new DefaultAttributeMetaData("entityA", FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData entityBCompound = new DefaultAttributeMetaData("entityB", FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData entityCCompound = new DefaultAttributeMetaData("entityC", FieldTypeEnum.COMPOUND);

		entityACompound.setAttributesMetaData(expectedEntityMetaA.getAtomicAttributes());
		entityBCompound.setAttributesMetaData(expectedEntityMetaB.getAtomicAttributes());
		entityCCompound.setAttributesMetaData(expectedEntityMetaC.getAtomicAttributes());
		expectedEntityMetaData.addAttributeMetaData(entityACompound, AttributeRole.ROLE_LOOKUP);
		expectedEntityMetaData.addAttributeMetaData(entityBCompound, AttributeRole.ROLE_LOOKUP);
		expectedEntityMetaData.addAttributeMetaData(entityCCompound, AttributeRole.ROLE_LOOKUP);

		// Mock the metadata
		when(dataService.getEntityMetaData("entityA")).thenReturn(entityMetaA);
		when(dataService.getEntityMetaData("entityB")).thenReturn(entityMetaB);
		when(dataService.getEntityMetaData("entityC")).thenReturn(entityMetaC);

		DefaultEntityMetaData emd = new DefaultEntityMetaData("MY_FIRST_VIEW", PackageImpl.defaultPackage);
		emd.addAttribute("id", AttributeRole.ROLE_ID, AttributeRole.ROLE_LABEL);
		emd.setAbstract(false);
		viewRepository = new ViewRepository(emd.getName(), dataService);

		Query queryMock = mock(Query.class);
		when(dataService.query(ViewMetaData.ENTITY_NAME)).thenReturn(queryMock);
		Query queryMock2 = mock(Query.class);
		when(queryMock.eq(ViewMetaData.NAME, "MY_FIRST_VIEW")).thenReturn(queryMock2);
		when(queryMock2.findOne()).thenReturn(viewMetaDataEntity);

		when(queryMock2.findAll()).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Arrays.asList(viewMetaDataEntity, viewMetaDataEntity2, viewMetaDataEntity3, viewMetaDataEntity4)
						.stream();
			}
		});
	}

	@Test
	private void getEntityMetaDataTest()
	{
		assertEquals(viewRepository.getEntityMetaData(), expectedEntityMetaData);
		assertEquals(viewRepository.getEntityMetaData().getAttributes().toString(),
				expectedEntityMetaData.getAttributes().toString());
	}

	@Test
	private void findAllTest()
	{
		Entity entityA1 = new MapEntity(entityMetaA);
		entityA1.set("id", "1");
		entityA1.set("chrom", "1");
		entityA1.set("pos", "25");
		entityA1.set("A", "testA1");

		Entity entityA2 = new MapEntity(entityMetaA);
		entityA2.set("id", "2");
		entityA2.set("chrom", "2");
		entityA2.set("pos", "50");
		entityA2.set("A", "testA2");

		Entity entityA3 = new MapEntity(entityMetaA);
		entityA3.set("id", "3");
		entityA3.set("chrom", "3");
		entityA3.set("pos", "75");
		entityA3.set("A", "testA3");

		Entity entityB1 = new MapEntity(entityMetaB);
		entityB1.set("id", "1");
		entityB1.set("chrom", "1");
		entityB1.set("pos", "25");
		entityB1.set("B", "testB1");

		Entity entityB2 = new MapEntity(entityMetaB);
		entityB2.set("id", "2");
		entityB2.set("chrom", "2");
		entityB2.set("pos", "50");
		entityB2.set("B", "testB2");

		Entity entityB3 = new MapEntity(entityMetaB);
		entityB3.set("id", "3");
		entityB3.set("chrom", "3");
		entityB3.set("pos", "75");
		entityB3.set("B", "testB3");

		Entity entityC1 = new MapEntity(entityMetaC);
		entityC1.set("id", "1");
		entityC1.set("chrom", "1");
		entityC1.set("pos", "25");
		entityC1.set("C", "testC1");

		Entity entityC2 = new MapEntity(entityMetaC);
		entityC2.set("id", "2");
		entityC2.set("chrom", "2");
		entityC2.set("pos", "50");
		entityC2.set("C", "testC2");

		Entity entityC3 = new MapEntity(entityMetaC);
		entityC3.set("id", "3");
		entityC3.set("chrom", "3");
		entityC3.set("pos", "75");
		entityC3.set("C", "testC3");

		// populate repositories
		repoA.add(entityA1);
		repoA.add(entityA2);
		repoA.add(entityA3);

		repoB.add(entityB1);
		repoB.add(entityB2);
		repoB.add(entityB3);

		repoC.add(entityC1);
		repoC.add(entityC2);
		repoC.add(entityC3);

		when(dataService.getRepository("entityA")).thenReturn(repoA);
		when(dataService.getRepository("EntityB")).thenReturn(repoB);
		when(dataService.getRepository("entityC")).thenReturn(repoC);

		Query q1 = new QueryImpl();
		q1.eq("chrom", "1");
		q1.and();
		q1.eq("pos", "25");
		when(dataService.findOne("entityB", q1)).thenReturn(entityB1);
		when(dataService.findOne("entityC", q1)).thenReturn(entityC1);

		Query q2 = new QueryImpl();
		q2.eq("chrom", "2");
		q2.and();
		q2.eq("pos", "50");
		when(dataService.findOne("entityB", q2)).thenReturn(entityB2);
		when(dataService.findOne("entityC", q2)).thenReturn(entityC2);

		Query q3 = new QueryImpl();
		q3.eq("chrom", "3");
		q3.and();
		q3.eq("pos", "75");
		when(dataService.findOne("entityB", q3)).thenReturn(entityB3);
		when(dataService.findOne("entityC", q3)).thenReturn(entityC3);

		DefaultEntity expected1 = new DefaultEntity(expectedEntityMetaData, dataService);
		expected1.set("id", "1");
		expected1.set("chrom", "1");
		expected1.set("pos", "25");
		expected1.set("A", "testA1");
		expected1.set("entityB_id", "1");
		expected1.set("entityB_chrom", "1");
		expected1.set("entityB_pos", "25");
		expected1.set("entityB_B", "testB1");
		expected1.set("entityC_id", "1");
		expected1.set("entityC_chrom", "1");
		expected1.set("entityC_pos", "25");
		expected1.set("entityC_C", "testC1");

		DefaultEntity expected2 = new DefaultEntity(expectedEntityMetaData, dataService);
		expected2.set("id", "2");
		expected2.set("chrom", "2");
		expected2.set("pos", "50");
		expected2.set("A", "testA2");
		expected2.set("entityB_id", "2");
		expected2.set("entityB_chrom", "2");
		expected2.set("entityB_pos", "50");
		expected2.set("entityB_B", "testB2");
		expected2.set("entityC_id", "2");
		expected2.set("entityC_chrom", "2");
		expected2.set("entityC_pos", "50");
		expected2.set("entityC_C", "testC2");

		DefaultEntity expected3 = new DefaultEntity(expectedEntityMetaData, dataService);
		expected3.set("id", "3");
		expected3.set("chrom", "3");
		expected3.set("pos", "75");
		expected3.set("A", "testA3");
		expected3.set("entityB_id", "3");
		expected3.set("entityB_chrom", "3");
		expected3.set("entityB_pos", "75");
		expected3.set("entityB_B", "testB3");
		expected3.set("entityC_id", "3");
		expected3.set("entityC_chrom", "3");
		expected3.set("entityC_pos", "75");
		expected3.set("entityC_C", "testC3");

		List<Entity> expected = newArrayList(expected1, expected2, expected3);
		List<Entity> actual = viewRepository.findAll(new QueryImpl()).collect(Collectors.toList());
		assertEquals(actual, expected);
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}

}
