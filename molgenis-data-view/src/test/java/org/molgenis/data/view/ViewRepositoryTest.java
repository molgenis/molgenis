package org.molgenis.data.view;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
//import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.assertFalse;
//import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
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
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
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

	@Autowired
	private SearchService searchService;

	private EditableEntityMetaData entityMetaA;
	private EditableEntityMetaData entityMetaB;
	private EditableEntityMetaData entityMetaC;

	private Repository repoA;
	private Repository repoB;
	private Repository repoC;
	private ViewRepository viewRepository;

	@BeforeClass
	public void setupBeforeClass()
	{
		// entity A (master entity)
		entityMetaA = new DefaultEntityMetaData("entityA");
		entityMetaA.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaA.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaA.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaA.addAttribute("A1").setDataType(STRING);

		// entity B (slave entity)
		entityMetaB = new DefaultEntityMetaData("entityB");
		entityMetaB.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaB.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaB.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaB.addAttribute("B1").setDataType(STRING);

		// entity C (slave entity)
		entityMetaC = new DefaultEntityMetaData("entityC");
		entityMetaC.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaC.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaC.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaC.addAttribute("C1").setDataType(STRING);

		// make repositories
		repoA = new InMemoryRepository(entityMetaA);
		repoB = new InMemoryRepository(entityMetaB);
		repoC = new InMemoryRepository(entityMetaC);

		Entity entityA1 = new MapEntity(entityMetaA);
		entityA1.set("id", "1");
		entityA1.set("chrom", "1");
		entityA1.set("pos", "25");
		entityA1.set("A1", "testA1");

		Entity entityA2 = new MapEntity(entityMetaA);
		entityA2.set("id", "2");
		entityA2.set("chrom", "2");
		entityA2.set("pos", "50");
		entityA2.set("A1", "testA2");

		Entity entityA3 = new MapEntity(entityMetaA);
		entityA3.set("id", "3");
		entityA3.set("chrom", "3");
		entityA3.set("pos", "75");
		entityA3.set("A1", "testA3");

		Entity entityB1 = new MapEntity(entityMetaB);
		entityB1.set("id", "1");
		entityB1.set("chrom", "1");
		entityB1.set("pos", "25");
		entityB1.set("B1", "testB1");

		Entity entityB2 = new MapEntity(entityMetaB);
		entityB2.set("id", "2");
		entityB2.set("chrom", "2");
		entityB2.set("pos", "50");
		entityB2.set("B1", "testA2");

		Entity entityB3 = new MapEntity(entityMetaB);
		entityB3.set("id", "3");
		entityB3.set("chrom", "3");
		entityB3.set("pos", "75");
		entityB3.set("B1", "testA3");

		Entity entityC1 = new MapEntity(entityMetaC);
		entityC1.set("id", "1");
		entityC1.set("chrom", "1");
		entityC1.set("pos", "25");
		entityC1.set("C1", "testB1");

		Entity entityC2 = new MapEntity(entityMetaC);
		entityC2.set("id", "2");
		entityC2.set("chrom", "2");
		entityC2.set("pos", "50");
		entityC2.set("C2", "testA2");

		Entity entityC3 = new MapEntity(entityMetaC);
		entityC3.set("id", "3");
		entityC3.set("chrom", "3");
		entityC3.set("pos", "75");
		entityC3.set("C3", "testA3");

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
	}

	@Test
	private void getEntityMetaData()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("MY_FIRST_VIEW", PackageImpl.defaultPackage);
		emd.addAttribute("id", AttributeRole.ROLE_ID, AttributeRole.ROLE_LABEL);
		emd.setAbstract(false);
		viewRepository = new ViewRepository(emd, dataService, searchService);

		when(dataService.getEntityMetaData("entityA")).thenReturn(entityMetaA);
		when(dataService.getEntityMetaData("entityB")).thenReturn(entityMetaB);
		when(dataService.getEntityMetaData("entityC")).thenReturn(entityMetaC);

		Query queryMock = mock(Query.class);
		when(dataService.query(EntityViewMetaData.ENTITY_NAME)).thenReturn(queryMock);
		Query queryMock2 = mock(Query.class);
		when(queryMock.eq(EntityViewMetaData.VIEW_NAME, "MY_FIRST_VIEW")).thenReturn(queryMock2);
		Entity entityA = mock(Entity.class);
		when(queryMock2.findOne()).thenReturn(entityA);
		when(entityA.getString(EntityViewMetaData.MASTER_ENTITY)).thenReturn("entityA");

		Entity entityB = mock(Entity.class);
		when(entityB.getString(EntityViewMetaData.JOIN_ENTITY)).thenReturn("entityB");
		Entity entityC = mock(Entity.class);
		when(entityC.getString(EntityViewMetaData.JOIN_ENTITY)).thenReturn("entityC");
		when(queryMock2.findAll()).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Arrays.asList(entityB, entityC).stream();
			}
		});

		DefaultEntityMetaData expectedEntityMetaData = new DefaultEntityMetaData("MY_FIRST_VIEW",
				PackageImpl.defaultPackage);
		expectedEntityMetaData.addAttribute("id", AttributeRole.ROLE_ID, AttributeRole.ROLE_LABEL);
		expectedEntityMetaData.setAbstract(false);
		DefaultAttributeMetaData entityACompound = new DefaultAttributeMetaData("entityA", FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData entityBCompound = new DefaultAttributeMetaData("entityB", FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData entityCCompound = new DefaultAttributeMetaData("entityC", FieldTypeEnum.COMPOUND);
		entityACompound.setAttributesMetaData(entityMetaA.getAttributes());
		entityBCompound.setAttributesMetaData(entityMetaB.getAttributes());
		entityCCompound.setAttributesMetaData(entityMetaC.getAttributes());
		expectedEntityMetaData.addAttributeMetaData(entityACompound, AttributeRole.ROLE_LOOKUP);
		expectedEntityMetaData.addAttributeMetaData(entityBCompound, AttributeRole.ROLE_LOOKUP);
		expectedEntityMetaData.addAttributeMetaData(entityCCompound, AttributeRole.ROLE_LOOKUP);

		assertEquals(viewRepository.getEntityMetaData(), expectedEntityMetaData);
		assertEquals(viewRepository.getEntityMetaData().getAttributes().toString(), expectedEntityMetaData
				.getAttributes().toString());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public LanguageService languageService()
		{
			return new LanguageService(dataService(), Mockito.mock(AppSettings.class));
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public SearchService searchService()
		{
			return mock(SearchService.class);
		}
	}

}
