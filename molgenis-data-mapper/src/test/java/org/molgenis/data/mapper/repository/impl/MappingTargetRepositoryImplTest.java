package org.molgenis.data.mapper.repository.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
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
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@ContextConfiguration(classes =
{ MappingTargetRepositoryImplTest.Config.class, MappingConfig.class })
public class MappingTargetRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;

	@Autowired
	private AttributeMappingRepositoryImpl attributeMappingRepository;

	@Autowired
	private EntityMappingRepositoryImpl entityMappingRepository;

	@Autowired
	private MappingTargetRepositoryImpl mappingTargetRepository;

	private static final String AUTO_ID = "1";

	@Test
	public void testToMappingTargets()
	{
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("targetAttribute");
		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("target");
		targetEntityMetaData.addAttributeMetaData(targetAttributeMetaData);

		List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
		attributeMappings.add(new AttributeMapping(AUTO_ID, targetAttributeMetaData, "algorithm"));

		List<EntityMapping> entityMappings = Arrays.asList(new EntityMapping(AUTO_ID, sourceEntityMetaData,
				targetEntityMetaData, attributeMappings));

		List<MappingTarget> mappingTargets = Arrays.asList(new MappingTarget(AUTO_ID, targetEntityMetaData,
				entityMappings));

		List<Entity> mappingTargetEntities = new ArrayList<Entity>();
		Entity mappingTargetEntity = new MapEntity(new MappingTargetMetaData());
		mappingTargetEntity.set(MappingTargetMetaData.IDENTIFIER, AUTO_ID);
		mappingTargetEntity.set(MappingTargetMetaData.TARGET, targetEntityMetaData);
		mappingTargetEntity.set(MappingTargetMetaData.ENTITYMAPPINGS, entityMappings);

		mappingTargetEntities.add(mappingTargetEntity);

		when(dataService.getEntityMetaData(mappingTargetEntity.getString(MappingTargetMetaData.TARGET))).thenReturn(
				targetEntityMetaData);

		assertEquals(mappingTargetRepository.toMappingTargets(mappingTargetEntities), mappingTargets);
	}

	private MappingTarget toMappingTarget(Entity mappingTargetEntity)
	{
		List<EntityMapping> entityMappings = Collections.emptyList();
		String identifier = mappingTargetEntity.getString(MappingTargetMetaData.IDENTIFIER);
		EntityMetaData target = dataService.getEntityMetaData(mappingTargetEntity
				.getString(MappingTargetMetaData.TARGET));
		if (mappingTargetEntity.getEntities(MappingTargetMetaData.ENTITYMAPPINGS) != null)
		{
			List<Entity> entityMappingEntities = Lists.newArrayList(mappingTargetEntity
					.getEntities(MappingTargetMetaData.ENTITYMAPPINGS));
			entityMappings = entityMappingRepository.toEntityMappings(entityMappingEntities);
		}
		return new MappingTarget(identifier, target, entityMappings);
	}

	@Test
	public void testUpsert()
	{
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
		AttributeMappingRepositoryImpl attributeMappingRepository()
		{
			return new AttributeMappingRepositoryImpl(dataService());
		}

		@Bean
		EntityMappingRepositoryImpl entityMappingRepository()
		{
			return new EntityMappingRepositoryImpl(attributeMappingRepository());
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
	}
}
