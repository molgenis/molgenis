package org.molgenis.data.view.repository;

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

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData.AttributeRole;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.ViewMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import autovalue.shaded.com.google.common.common.collect.Lists;

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
	private Repository repoEvmd;

	private Entity entityEvmd1;
	private Entity entityEvmd2;
	private Entity entityEvmd3;
	private Entity entityEvmd4;

	@BeforeClass
	public void setupBeforeClass()
	{
		// entity A (master entity)
		entityMetaA = new DefaultEntityMetaData("entityA");
		entityMetaA.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaA.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaA.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaA.addAttribute("A").setDataType(STRING);

		// entity B (slave entity)
		entityMetaB = new DefaultEntityMetaData("entityB");
		entityMetaB.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaB.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaB.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaB.addAttribute("B").setDataType(STRING);

		// entity C (slave entity)
		entityMetaC = new DefaultEntityMetaData("entityC");
		entityMetaC.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaC.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaC.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaC.addAttribute("C").setDataType(STRING);

		ViewMetaData evmd = new ViewMetaData();

		// make repositories
		repoA = new InMemoryRepository(entityMetaA);
		repoB = new InMemoryRepository(entityMetaB);
		repoC = new InMemoryRepository(entityMetaC);
		repoEvmd = new InMemoryRepository(evmd);

		entityEvmd1 = new MapEntity(evmd);
		entityEvmd1.set("id", "1");
		entityEvmd1.set(ViewMetaData.NAME, "MY_FIRST_VIEW");
		entityEvmd1.set(ViewMetaData.MASTER_ENTITY, "entityA");
		entityEvmd1.set(ViewMetaData.MASTER_ATTR, "chrom");
		entityEvmd1.set(ViewMetaData.JOIN_ENTITY, "entityB");
		entityEvmd1.set(ViewMetaData.JOIN_ATTR, "chrom");
		repoEvmd.add(entityEvmd1);

		entityEvmd2 = new MapEntity(evmd);
		entityEvmd2.set("id", "2");
		entityEvmd2.set(ViewMetaData.VIEW_NAME, "MY_FIRST_VIEW");
		entityEvmd2.set(ViewMetaData.MASTER_ENTITY, "entityA");
		entityEvmd2.set(ViewMetaData.MASTER_ATTR, "pos");
		entityEvmd2.set(ViewMetaData.JOIN_ENTITY, "entityB");
		entityEvmd2.set(ViewMetaData.JOIN_ATTR, "pos");
		repoEvmd.add(entityEvmd2);

		entityEvmd3 = new MapEntity(evmd);
		entityEvmd3.set("id", "3");
		entityEvmd3.set(ViewMetaData.VIEW_NAME, "MY_FIRST_VIEW");
		entityEvmd3.set(ViewMetaData.MASTER_ENTITY, "entityA");
		entityEvmd3.set(ViewMetaData.MASTER_ATTR, "chrom");
		entityEvmd3.set(ViewMetaData.JOIN_ENTITY, "entityC");
		entityEvmd3.set(ViewMetaData.JOIN_ATTR, "chrom");
		repoEvmd.add(entityEvmd3);

		entityEvmd4 = new MapEntity(evmd);
		entityEvmd4.set("id", "4");
		entityEvmd4.set(ViewMetaData.VIEW_NAME, "MY_FIRST_VIEW");
		entityEvmd4.set(ViewMetaData.MASTER_ENTITY, "entityA");
		entityEvmd4.set(ViewMetaData.MASTER_ATTR, "pos");
		entityEvmd4.set(ViewMetaData.JOIN_ENTITY, "entityC");
		entityEvmd4.set(ViewMetaData.JOIN_ATTR, "pos");
		repoEvmd.add(entityEvmd4);

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
		viewRepository = new ViewRepository(emd, dataService);

		Query queryMock = mock(Query.class);
		when(dataService.query(ViewMetaData.ENTITY_NAME)).thenReturn(queryMock);
		Query queryMock2 = mock(Query.class);
		when(queryMock.eq(ViewMetaData.VIEW_NAME, "MY_FIRST_VIEW")).thenReturn(queryMock2);
		when(queryMock2.findOne()).thenReturn(entityEvmd1);

		when(queryMock2.findAll()).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Arrays.asList(entityEvmd1, entityEvmd2, entityEvmd3, entityEvmd4).stream();
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

		Query q1 = new QueryImpl();
		q1.eq("chrom", "1");
		q1.and();
		q1.eq("pos", "25");
		when(dataService.findAll("entityB", q1)).thenReturn(Arrays.asList(entityB1).stream());

		Query q2 = new QueryImpl();
		q2.eq("chrom", "2");
		q2.and();
		q2.eq("pos", "50");
		when(dataService.findAll("entityB", q2)).thenReturn(Arrays.asList(entityB2).stream());

		Query q3 = new QueryImpl();
		q3.eq("chrom", "3");
		q3.and();
		q3.eq("pos", "75");
		when(dataService.findAll("entityB", q3)).thenReturn(Arrays.asList(entityB3).stream());

		Query q4 = new QueryImpl();
		q4.eq("chrom", "1");
		q4.and();
		q4.eq("pos", "25");
		when(dataService.findAll("entityC", q4)).thenReturn(Arrays.asList(entityC1).stream());

		Query q5 = new QueryImpl();
		q5.eq("chrom", "2");
		q5.and();
		q5.eq("pos", "50");
		when(dataService.findAll("entityC", q2)).thenReturn(Arrays.asList(entityC2).stream());

		Query q6 = new QueryImpl();
		q6.eq("chrom", "3");
		q6.and();
		q6.eq("pos", "75");
		when(dataService.findAll("entityC", q6)).thenReturn(Arrays.asList(entityC3).stream());

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

		List<Entity> expected = Lists.newArrayList(expected1, expected2, expected3);
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
