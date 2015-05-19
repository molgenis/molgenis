package org.molgenis.data.mapper.repository.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.mapper.meta.MappingTargetMetaData.ENTITYMAPPINGS;
import static org.molgenis.data.mapper.meta.MappingTargetMetaData.IDENTIFIER;
import static org.molgenis.data.mapper.meta.MappingTargetMetaData.TARGET;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.EntityMappingMetaData;
import org.molgenis.data.mapper.repository.EntityMappingRepository;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for the MappingTargetRepository. Tests the MappingTargetRepository in isolation.
 */
@ContextConfiguration(classes =
{ MappingTargetRepositoryImplTest.Config.class })
public class MappingTargetRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;

	@Autowired
	private EntityMappingRepository entityMappingRepository;

	@Autowired
	private MappingTargetRepositoryImpl mappingTargetRepository;

	@Autowired
	private IdGenerator idGenerator;

	private List<MappingTarget> mappingTargets;

	private List<Entity> mappingTargetEntities;

	private DefaultEntityMetaData targetEntityMetaData;

	private List<Entity> entityMappingEntities;

	private List<EntityMapping> entityMappings;

	@Captor
	ArgumentCaptor<Collection<EntityMapping>> entityMappingCaptor;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);

		// POJOs
		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		targetEntityMetaData = new DefaultEntityMetaData("target");
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("targetAttribute");
		targetEntityMetaData.addAttributeMetaData(targetAttributeMetaData);
		entityMappings = Arrays.asList(new EntityMapping("entityMappingID", sourceEntityMetaData, targetEntityMetaData,
				emptyList()));
		mappingTargets = Arrays.asList(new MappingTarget("mappingTargetID", targetEntityMetaData, entityMappings));

		// Entities
		Entity entityMappingEntity = new MapEntity(EntityMappingRepositoryImpl.META_DATA);
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, "entityMappingID");
		entityMappingEntity.set(EntityMappingMetaData.SOURCEENTITYMETADATA, "source");
		entityMappingEntity.set(EntityMappingMetaData.TARGETENTITYMETADATA, "target");
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTEMAPPINGS, emptyList());
		Entity mappingTargetEntity = new MapEntity(MappingTargetRepositoryImpl.META_DATA);
		mappingTargetEntity.set(IDENTIFIER, "mappingTargetID");
		mappingTargetEntity.set(TARGET, "target");

		entityMappingEntities = asList(entityMappingEntity);
		mappingTargetEntity.set(ENTITYMAPPINGS, entityMappingEntities);

		mappingTargetEntities = asList(mappingTargetEntity);
	}

	@Test
	public void testToMappingTargets()
	{
		when(dataService.getEntityMetaData("target")).thenReturn(targetEntityMetaData);
		when(entityMappingRepository.toEntityMappings(entityMappingEntities)).thenReturn(entityMappings);
		when(dataService.hasRepository("target")).thenReturn(true);

		assertEquals(mappingTargetRepository.toMappingTargets(mappingTargetEntities), mappingTargets);
	}

	@Test
	public void testUpdate()
	{
		when(entityMappingRepository.upsert(entityMappings)).thenReturn(entityMappingEntities);
		List<Entity> result = mappingTargetRepository.upsert(mappingTargets);
		assertEquals(result, mappingTargetEntities);
	}

	@Test
	public void testInsert()
	{
		mappingTargets.get(0).setIdentifier(null);

		when(idGenerator.generateId()).thenReturn("mappingTargetID");
		when(entityMappingRepository.upsert(entityMappings)).thenReturn(entityMappingEntities);
		List<Entity> result = mappingTargetRepository.upsert(mappingTargets);
		assertEquals(result, mappingTargetEntities);
	}

	@Configuration
	public static class Config
	{
		@Bean
		DataServiceImpl dataService()
		{
			return mock(DataServiceImpl.class);
		}

		@Bean
		EntityMappingRepository entityMappingRepository()
		{
			return mock(EntityMappingRepository.class);
		}

		@Bean
		MappingTargetRepositoryImpl mappingTargetRepository()
		{
			return new MappingTargetRepositoryImpl(entityMappingRepository());
		}

		@Bean
		MolgenisUserService userService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}
	}
}
