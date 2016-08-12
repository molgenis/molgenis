package org.molgenis.data.mapper.repository.impl;

import org.mockito.ArgumentCaptor;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserFactory;
import org.molgenis.data.*;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.MappingProjectMetaData;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
import org.molgenis.data.mapper.repository.MappingTargetRepository;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.TagMetaData.TAG;
import static org.testng.Assert.*;

@ContextConfiguration(classes = MappingProjectRepositoryImplTest.Config.class)
public class MappingProjectRepositoryImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private MappingProjectRepositoryImpl mappingProjectRepositoryImpl;

	@Autowired
	private MolgenisUserFactory molgenisUserFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private MappingTargetRepository mappingTargetRepository;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private MolgenisUserService userService;

	@Autowired
	private MappingProjectMetaData mappingProjectMeta;

	@Autowired
	private MappingTargetMetaData mappingTargetMeta;

	private MolgenisUser owner;

	private MappingTarget mappingTarget1;

	private MappingTarget mappingTarget2;

	private List<Entity> mappingTargetEntities;

	private MappingProject mappingProject;

	private Entity mappingProjectEntity;

	@BeforeMethod
	public void beforeMethod()
	{
		owner = molgenisUserFactory.create();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		EntityMetaData target1 = entityMetaFactory.create("target1");
		target1.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		EntityMetaData target2 = entityMetaFactory.create("target2");
		target2.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);

		mappingProject = new MappingProject("My first mapping project", owner);
		mappingTarget1 = mappingProject.addTarget(target1);
		mappingTarget2 = mappingProject.addTarget(target2);

		Entity mappingTargetEntity = new DynamicEntity(mappingTargetMeta);
		mappingTargetEntity.set(MappingTargetMetaData.TARGET, "target1");
		mappingTargetEntity.set(MappingTargetMetaData.IDENTIFIER, "mappingTargetID1");
		Entity mappingTargetEntity2 = new DynamicEntity(mappingTargetMeta);
		mappingTargetEntity2.set(MappingTargetMetaData.TARGET, "target2");
		mappingTargetEntity2.set(MappingTargetMetaData.IDENTIFIER, "mappingTargetID2");
		mappingTargetEntities = asList(mappingTargetEntity, mappingTargetEntity2);

		mappingProjectEntity = new DynamicEntity(mappingProjectMeta);
		mappingProjectEntity.set(IDENTIFIER, "mappingProjectID");
		mappingProjectEntity.set(MAPPING_TARGETS, mappingTargetEntities);
		mappingProjectEntity.set(OWNER, owner);
		mappingProjectEntity.set(NAME, "My first mapping project");
	}

	@Test
	public void testAdd()
	{
		when(idGenerator.generateId()).thenReturn("mappingProjectID");
		when(mappingTargetRepository.upsert(asList(mappingTarget1, mappingTarget2))).thenReturn(mappingTargetEntities);

		mappingProjectRepositoryImpl.add(mappingProject);

		ArgumentCaptor<DynamicEntity> argumentCaptor = ArgumentCaptor.forClass(DynamicEntity.class);
		verify(dataService).add(eq(MAPPING_PROJECT), argumentCaptor.capture());
		assertEquals(argumentCaptor.getValue().getString(IDENTIFIER), "mappingProjectID");
		assertNull(mappingTarget1.getIdentifier());
		assertNull(mappingTarget2.getIdentifier());
	}

	@Test
	public void testAddWithIdentifier()
	{
		MappingProject mappingProject = new MappingProject("My first mapping project", owner);
		mappingProject.setIdentifier("mappingProjectID");
		try
		{
			mappingProjectRepositoryImpl.add(mappingProject);
		}
		catch (MolgenisDataException mde)
		{
			assertEquals(mde.getMessage(), "MappingProject already exists");
		}
	}

	@Test
	public void testDelete()
	{
		mappingProjectRepositoryImpl.delete("abc");
		verify(dataService).deleteById(MAPPING_PROJECT, "abc");
	}

	@Test
	public void testQuery()
	{
		Query<Entity> q = new QueryImpl<>();
		q.eq(OWNER, "flup");
		when(dataService.findAll(MAPPING_PROJECT, q)).thenReturn(Stream.of(mappingProjectEntity));
		when(userService.getUser("flup")).thenReturn(owner);
		when(mappingTargetRepository.toMappingTargets(mappingTargetEntities))
				.thenReturn(asList(mappingTarget1, mappingTarget2));
		List<MappingProject> result = mappingProjectRepositoryImpl.getMappingProjects(q);
		mappingProject.setIdentifier("mappingProjectID");
		assertEquals(result, singletonList(mappingProject));
	}

	@Test
	public void testFindAll()
	{
		Query<Entity> q = new QueryImpl<>();
		q.eq(OWNER, "flup");
		when(dataService.findAll(MAPPING_PROJECT)).thenReturn(Stream.of(mappingProjectEntity));
		when(userService.getUser("flup")).thenReturn(owner);
		when(mappingTargetRepository.toMappingTargets(mappingTargetEntities))
				.thenReturn(asList(mappingTarget1, mappingTarget2));
		List<MappingProject> result = mappingProjectRepositoryImpl.getAllMappingProjects();
		mappingProject.setIdentifier("mappingProjectID");
		assertEquals(result, singletonList(mappingProject));
	}

	@Test
	public void testUpdateUnknown()
	{
		mappingProject.setIdentifier("mappingProjectID");
		when(dataService.findOneById(TAG, "mappingProjectID")).thenReturn(null);
		try
		{
			mappingProjectRepositoryImpl.update(mappingProject);
			fail("Expected exception");
		}
		catch (MolgenisDataException expected)
		{
			assertEquals(expected.getMessage(), "MappingProject does not exist");
		}
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.mapper.meta", "org.molgenis.auth" })
	public static class Config
	{
		@Autowired
		private MappingProjectMetaData mappingProjectMeta;

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MappingTargetRepository mappingTargetRepository()
		{
			return mock(MappingTargetRepository.class);
		}

		@Bean
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		public MappingProjectRepositoryImpl mappingProjectRepositoryImpl()
		{
			return new MappingProjectRepositoryImpl(dataService(), mappingTargetRepository(), molgenisUserService(),
					idGenerator(), mappingProjectMeta);
		}

	}
}
