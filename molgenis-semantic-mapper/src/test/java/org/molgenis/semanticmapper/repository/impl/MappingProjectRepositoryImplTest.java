package org.molgenis.semanticmapper.repository.impl;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.semanticmapper.config.MapperTestConfig;
import org.molgenis.semanticmapper.exception.DuplicateMappingProjectException;
import org.molgenis.semanticmapper.mapping.model.MappingProject;
import org.molgenis.semanticmapper.mapping.model.MappingTarget;
import org.molgenis.semanticmapper.meta.MappingProjectMetaData;
import org.molgenis.semanticmapper.meta.MappingTargetMetaData;
import org.molgenis.semanticmapper.repository.MappingTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.semanticmapper.meta.MappingProjectMetaData.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = MappingProjectRepositoryImplTest.Config.class)
public class MappingProjectRepositoryImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private MappingProjectRepositoryImpl mappingProjectRepositoryImpl;

	@Autowired
	private UserFactory userFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private MappingTargetRepository mappingTargetRepository;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private MappingProjectMetaData mappingProjectMeta;

	@Autowired
	private MappingTargetMetaData mappingTargetMeta;

	private User owner;

	private MappingTarget mappingTarget1;

	private MappingTarget mappingTarget2;

	private List<Entity> mappingTargetEntities;

	private MappingProject mappingProject;

	private Entity mappingProjectEntity;

	@BeforeMethod
	public void beforeMethod()
	{
		owner = userFactory.create();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		EntityType target1 = entityTypeFactory.create("target1");
		target1.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		EntityType target2 = entityTypeFactory.create("target2");
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

	@Test(expectedExceptions = DuplicateMappingProjectException.class, expectedExceptionsMessageRegExp = "id:mappingProjectID")
	public void testAddWithIdentifier()
	{
		MappingProject mappingProject = new MappingProject("My first mapping project", owner);
		mappingProject.setIdentifier("mappingProjectID");
		mappingProjectRepositoryImpl.add(mappingProject);
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
		when(mappingTargetRepository.toMappingTargets(mappingTargetEntities)).thenReturn(
				asList(mappingTarget1, mappingTarget2));
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
		when(mappingTargetRepository.toMappingTargets(mappingTargetEntities)).thenReturn(
				asList(mappingTarget1, mappingTarget2));
		List<MappingProject> result = mappingProjectRepositoryImpl.getAllMappingProjects();
		mappingProject.setIdentifier("mappingProjectID");
		assertEquals(result, singletonList(mappingProject));
	}

	@Test(expectedExceptions = UnknownEntityException.class, expectedExceptionsMessageRegExp = "type:sys_map_MappingProject id:mappingProjectID")
	public void testUpdateUnknown()
	{
		mappingProject.setIdentifier("mappingProjectID");
		when(dataService.findOneById(TAG, "mappingProjectID")).thenReturn(null);
		mappingProjectRepositoryImpl.update(mappingProject);
	}

	@Configuration
	@Import(MapperTestConfig.class)
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Autowired
		private MappingProjectMetaData mappingProjectMeta;

		@Bean
		public MappingTargetRepository mappingTargetRepository()
		{
			return mock(MappingTargetRepository.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		public MappingProjectRepositoryImpl mappingProjectRepositoryImpl()
		{
			return new MappingProjectRepositoryImpl(dataService, mappingTargetRepository(), idGenerator(),
					mappingProjectMeta);
		}

	}
}
