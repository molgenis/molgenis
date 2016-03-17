package org.molgenis.data.mapper.repository.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.ENTITY_NAME;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.IDENTIFIER;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.MAPPINGTARGETS;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.NAME;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.OWNER;
import static org.molgenis.data.mapper.repository.impl.MappingProjectRepositoryImpl.META_DATA;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.util.List;
import java.util.stream.Stream;

import org.mockito.Mockito;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
import org.molgenis.data.mapper.repository.MappingTargetRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MappingProjectRepositoryImplTest.Config.class)
public class MappingProjectRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MappingProjectRepositoryImpl mappingProjectRepositoryImpl;

	@Autowired
	private DataService dataService;

	@Autowired
	private MappingTargetRepository mappingTargetRepository;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private MolgenisUserService userService;

	private MolgenisUser owner;

	private MappingTarget mappingTarget1;

	private MappingTarget mappingTarget2;

	private List<Entity> mappingTargetEntities;

	private MappingProject mappingProject;

	private Entity mappingProjectEntity;

	@BeforeMethod
	public void beforeMethod()
	{
		owner = new MolgenisUser();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		DefaultEntityMetaData target1 = new DefaultEntityMetaData("target1");
		target1.addAttribute("id", ROLE_ID);
		DefaultEntityMetaData target2 = new DefaultEntityMetaData("target2");
		target2.addAttribute("id", ROLE_ID);

		mappingProject = new MappingProject("My first mapping project", owner);
		mappingTarget1 = mappingProject.addTarget(target1);
		mappingTarget2 = mappingProject.addTarget(target2);

		Entity mappingTargetEntity = new MapEntity(MappingTargetRepositoryImpl.META_DATA);
		mappingTargetEntity.set(MappingTargetMetaData.TARGET, "target1");
		mappingTargetEntity.set(MappingTargetMetaData.IDENTIFIER, "mappingTargetID1");
		Entity mappingTargetEntity2 = new MapEntity(MappingTargetRepositoryImpl.META_DATA);
		mappingTargetEntity2.set(MappingTargetMetaData.TARGET, "target2");
		mappingTargetEntity2.set(MappingTargetMetaData.IDENTIFIER, "mappingTargetID2");
		mappingTargetEntities = asList(mappingTargetEntity, mappingTargetEntity2);

		mappingProjectEntity = new MapEntity(META_DATA);
		mappingProjectEntity.set(IDENTIFIER, "mappingProjectID");
		mappingProjectEntity.set(MAPPINGTARGETS, mappingTargetEntities);
		mappingProjectEntity.set(OWNER, owner);
		mappingProjectEntity.set(NAME, "My first mapping project");
	}

	@Test
	public void testAdd()
	{
		when(idGenerator.generateId()).thenReturn("mappingProjectID");
		when(mappingTargetRepository.upsert(asList(mappingTarget1, mappingTarget2))).thenReturn(mappingTargetEntities);

		mappingProjectRepositoryImpl.add(mappingProject);

		Mockito.verify(dataService).add(ENTITY_NAME, mappingProjectEntity);
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
		verify(dataService).delete(ENTITY_NAME, "abc");
	}

	@Test
	public void testQuery()
	{
		Query q = new QueryImpl();
		q.eq(OWNER, "flup");
		when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Stream.of(mappingProjectEntity));
		when(userService.getUser("flup")).thenReturn(owner);
		when(mappingTargetRepository.toMappingTargets(mappingTargetEntities))
				.thenReturn(asList(mappingTarget1, mappingTarget2));
		List<MappingProject> result = mappingProjectRepositoryImpl.getMappingProjects(q);
		mappingProject.setIdentifier("mappingProjectID");
		assertEquals(result, asList(mappingProject));
	}

	@Test
	public void testFindAll()
	{
		Query q = new QueryImpl();
		q.eq(OWNER, "flup");
		when(dataService.findAll(ENTITY_NAME)).thenReturn(Stream.of(mappingProjectEntity));
		when(userService.getUser("flup")).thenReturn(owner);
		when(mappingTargetRepository.toMappingTargets(mappingTargetEntities))
				.thenReturn(asList(mappingTarget1, mappingTarget2));
		List<MappingProject> result = mappingProjectRepositoryImpl.getAllMappingProjects();
		mappingProject.setIdentifier("mappingProjectID");
		assertEquals(result, asList(mappingProject));
	}

	@Test
	public void testUpdateUnknown()
	{
		mappingProject.setIdentifier("mappingProjectID");
		when(dataService.findOne(ENTITY_NAME, "mappingProjectID")).thenReturn(null);
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
	public static class Config
	{
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
			return new MappingProjectRepositoryImpl(dataService(), mappingTargetRepository());
		}

	}
}
