package org.molgenis.data.mapper.repository.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.mapper.meta.EntityMappingMetaData;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ EntityMappingRepositoryImplTest.Config.class, MappingConfig.class })
public class EntityMappingRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;

	@Autowired
	private AttributeMappingRepositoryImpl attributeMappingRepository;

	@Autowired
	private EntityMappingRepositoryImpl entityMappingRepository;

	private static final String AUTO_ID = "1";

	@Test
	public void testToEntityMappings()
	{
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("targetAttribute");
		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();
		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("target");
		targetEntityMetaData.addAttributeMetaData(targetAttributeMetaData);

		List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
		attributeMappings.add(new AttributeMapping("1", targetAttributeMetaData, "algorithm", sourceAttributeMetaDatas));

		List<EntityMapping> entityMappings = Arrays.asList(new EntityMapping(AUTO_ID, sourceEntityMetaData,
				targetEntityMetaData, attributeMappings));

		Entity attributeMappingEntity = new MapEntity(new AttributeMappingMetaData());
		attributeMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		attributeMappingEntity.set(AttributeMappingMetaData.TARGETATTRIBUTEMETADATA, "targetAttribute");
		attributeMappingEntity.set(AttributeMappingMetaData.SOURCEATTRIBUTEMETADATAS, "sourceAttributes");
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");

		List<Entity> attributeMappingEntities = new ArrayList<Entity>();
		attributeMappingEntities.add(attributeMappingEntity);

		List<Entity> entityMappingEntities = new ArrayList<Entity>();
		Entity entityMappingEntity = new MapEntity(new EntityMappingMetaData());
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		entityMappingEntity.set(EntityMappingMetaData.TARGETENTITYMETADATA, "targetAttribute");
		entityMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTEMAPPINGS, attributeMappingEntities);

		entityMappingEntities.add(entityMappingEntity);

		when(dataService.getEntityMetaData(entityMappingEntity.getString(EntityMappingMetaData.TARGETENTITYMETADATA)))
				.thenReturn(targetEntityMetaData);
		when(dataService.getEntityMetaData(entityMappingEntity.getString(EntityMappingMetaData.SOURCEENTITYMETADATA)))
				.thenReturn(sourceEntityMetaData);

		assertEquals(entityMappingRepository.toEntityMappings(entityMappingEntities), entityMappings);
	}

	@Test
	public void testUpsert()
	{
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("targetAttribute");
		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();
		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("target");
		targetEntityMetaData.addAttributeMetaData(targetAttributeMetaData);

		List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
		attributeMappings.add(new AttributeMapping("1", targetAttributeMetaData, "algorithm", sourceAttributeMetaDatas));

		Collection<EntityMapping> entityMappings = Arrays.asList(new EntityMapping(AUTO_ID, sourceEntityMetaData,
				targetEntityMetaData, attributeMappings));

		Entity attributeMappingEntity = new MapEntity(new AttributeMappingMetaData());
		attributeMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		attributeMappingEntity.set(AttributeMappingMetaData.TARGETATTRIBUTEMETADATA, "targetAttribute");
		attributeMappingEntity.set(AttributeMappingMetaData.SOURCEATTRIBUTEMETADATAS, sourceAttributeMetaDatas);
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");

		List<Entity> attributeMappingEntities = new ArrayList<Entity>();
		attributeMappingEntities.add(attributeMappingEntity);

		List<Entity> entityMappingEntities = new ArrayList<Entity>();
		Entity entityMappingEntity = new MapEntity(new EntityMappingMetaData());
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		entityMappingEntity.set(EntityMappingMetaData.SOURCEENTITYMETADATA, "source");
		entityMappingEntity.set(EntityMappingMetaData.TARGETENTITYMETADATA, "target");
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTEMAPPINGS, attributeMappingEntities);
		entityMappingEntities.add(entityMappingEntity);

		assertEquals(entityMappingRepository.upsert(entityMappings).get(0), entityMappingEntities.get(0));

		verify(dataService).update(EntityMappingRepositoryImpl.META_DATA.getName(), entityMappingEntity);
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
		SemanticSearchService semanticSearchService()
		{
			return mock(SemanticSearchService.class);
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
			return new UuidGenerator();
		}
	}
}
