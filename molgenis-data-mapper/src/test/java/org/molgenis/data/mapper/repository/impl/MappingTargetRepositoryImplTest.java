package org.molgenis.data.mapper.repository.impl;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.EntityMappingMetaData;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
import org.molgenis.data.mapper.repository.EntityMappingRepository;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.mapper.meta.MappingTargetMetaData.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for the MappingTargetRepository. Tests the MappingTargetRepository in isolation.
 */
@ContextConfiguration(classes = { MappingTargetRepositoryImplTest.Config.class })
public class MappingTargetRepositoryImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private EntityMappingRepository entityMappingRepository;

	@Autowired
	private MappingTargetRepositoryImpl mappingTargetRepository;

	@Autowired
	private EntityMappingMetaData entityMappingMeta;

	@Autowired
	private MappingTargetMetaData mappingTargetMeta;

	@Autowired
	private IdGenerator idGenerator;

	private List<MappingTarget> mappingTargets;

	private List<Entity> mappingTargetEntities;

	private EntityMetaData targetEntityMetaData;

	private List<Entity> entityMappingEntities;

	private List<EntityMapping> entityMappings;

	@Captor
	ArgumentCaptor<Collection<EntityMapping>> entityMappingCaptor;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);

		// POJOs
		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		targetEntityMetaData = entityMetaFactory.create("target");
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("targetAttribute");
		targetEntityMetaData.addAttribute(targetAttributeMetaData);
		entityMappings = asList(
				new EntityMapping("entityMappingID", sourceEntityMetaData, targetEntityMetaData, emptyList()));
		mappingTargets = asList(new MappingTarget("mappingTargetID", targetEntityMetaData, entityMappings));

		// Entities
		Entity entityMappingEntity = new DynamicEntity(entityMappingMeta);
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, "entityMappingID");
		entityMappingEntity.set(EntityMappingMetaData.SOURCE_ENTITY_META_DATA, "source");
		entityMappingEntity.set(EntityMappingMetaData.TARGET_ENTITY_META_DATA, "target");
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTE_MAPPINGS, emptyList());
		Entity mappingTargetEntity = new DynamicEntity(mappingTargetMeta);
		mappingTargetEntity.set(IDENTIFIER, "mappingTargetID");
		mappingTargetEntity.set(TARGET, "target");

		entityMappingEntities = asList(entityMappingEntity);
		mappingTargetEntity.set(ENTITY_MAPPINGS, entityMappingEntities);

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

		assertEquals(mappingTargetEntities.size(), result.size());
		for (int i = 0; i < mappingTargetEntities.size(); ++i)
		{
			assertTrue(EntityUtils.equals(mappingTargetEntities.get(i), result.get(i)));
		}
	}

	@Test
	public void testInsert()
	{
		mappingTargets.get(0).setIdentifier(null);

		when(idGenerator.generateId()).thenReturn("mappingTargetID");
		when(entityMappingRepository.upsert(entityMappings)).thenReturn(entityMappingEntities);
		List<Entity> result = mappingTargetRepository.upsert(mappingTargets);

		assertEquals(mappingTargetEntities.size(), result.size());
		for (int i = 0; i < mappingTargetEntities.size(); ++i)
		{
			assertTrue(EntityUtils.equals(mappingTargetEntities.get(i), result.get(i)));
		}
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.mapper.meta", "org.molgenis.auth" })
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
