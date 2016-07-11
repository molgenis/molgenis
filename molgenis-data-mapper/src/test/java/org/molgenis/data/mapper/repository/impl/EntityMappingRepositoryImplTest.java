package org.molgenis.data.mapper.repository.impl;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.mapper.meta.EntityMappingMetaData;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@ContextConfiguration(classes = { EntityMappingRepositoryImplTest.Config.class, MappingConfig.class })
public class EntityMappingRepositoryImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private AttributeMappingMetaData attrMappingMeta;

	@Autowired
	private EntityMappingMetaData entityMappingMeta;

	@Autowired
	private DataService dataService;

	@Autowired
	private EntityMappingRepositoryImpl entityMappingRepository;

	private static final String AUTO_ID = "1";

	@Test
	public void testToEntityMappings()
	{
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("targetAttribute");
		List<AttributeMetaData> sourceAttributeMetaDatas = Lists.newArrayList();
		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		EntityMetaData targetEntityMetaData = entityMetaFactory.create("target");
		targetEntityMetaData.addAttribute(targetAttributeMetaData);

		List<AttributeMapping> attributeMappings = Lists.newArrayList();
		attributeMappings
				.add(new AttributeMapping("1", targetAttributeMetaData, "algorithm", sourceAttributeMetaDatas));

		List<EntityMapping> entityMappings = singletonList(
				new EntityMapping(AUTO_ID, sourceEntityMetaData, targetEntityMetaData, attributeMappings));

		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		attributeMappingEntity.set(AttributeMappingMetaData.TARGETATTRIBUTEMETADATA, "targetAttribute");
		attributeMappingEntity.set(AttributeMappingMetaData.SOURCEATTRIBUTEMETADATAS, "sourceAttributes");
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");

		List<Entity> attributeMappingEntities = Lists.newArrayList();
		attributeMappingEntities.add(attributeMappingEntity);

		List<Entity> entityMappingEntities = Lists.newArrayList();
		Entity entityMappingEntity = new DynamicEntity(entityMappingMeta);
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		entityMappingEntity.set(EntityMappingMetaData.TARGET_ENTITY_META_DATA, "targetAttribute");
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTE_MAPPINGS, attributeMappingEntities);

		entityMappingEntities.add(entityMappingEntity);

		when(dataService
				.getEntityMetaData(entityMappingEntity.getString(EntityMappingMetaData.TARGET_ENTITY_META_DATA)))
				.thenReturn(targetEntityMetaData);
		when(dataService
				.getEntityMetaData(entityMappingEntity.getString(EntityMappingMetaData.SOURCE_ENTITY_META_DATA)))
				.thenReturn(sourceEntityMetaData);

		assertEquals(entityMappingRepository.toEntityMappings(entityMappingEntities), entityMappings);
	}

	@Test
	public void testUpsert()
	{
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("targetAttribute");
		List<AttributeMetaData> sourceAttributeMetaDatas = Lists.newArrayList();
		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		EntityMetaData targetEntityMetaData = entityMetaFactory.create("target");
		targetEntityMetaData.addAttribute(targetAttributeMetaData);

		List<AttributeMapping> attributeMappings = Lists.newArrayList();
		attributeMappings
				.add(new AttributeMapping("1", targetAttributeMetaData, "algorithm", sourceAttributeMetaDatas));

		Collection<EntityMapping> entityMappings = singletonList(
				new EntityMapping(AUTO_ID, sourceEntityMetaData, targetEntityMetaData, attributeMappings));

		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		attributeMappingEntity.set(AttributeMappingMetaData.TARGETATTRIBUTEMETADATA, "targetAttribute");
		attributeMappingEntity.set(AttributeMappingMetaData.SOURCEATTRIBUTEMETADATAS, "");
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHMSTATE, null);

		List<Entity> attributeMappingEntities = Lists.newArrayList();
		attributeMappingEntities.add(attributeMappingEntity);

		List<Entity> entityMappingEntities = Lists.newArrayList();
		Entity entityMappingEntity = new DynamicEntity(entityMappingMeta);
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		entityMappingEntity.set(EntityMappingMetaData.SOURCE_ENTITY_META_DATA, "source");
		entityMappingEntity.set(EntityMappingMetaData.TARGET_ENTITY_META_DATA, "target");
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTE_MAPPINGS, attributeMappingEntities);
		entityMappingEntities.add(entityMappingEntity);

		assertTrue(EntityUtils
				.equals(entityMappingRepository.upsert(entityMappings).get(0), entityMappingEntities.get(0)));
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.mapper.meta", "org.molgenis.auth" })
	public static class Config
	{
		@Autowired
		private AttributeMappingMetaData attrMappingMeta;

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
			return new AttributeMappingRepositoryImpl(dataService(), attrMappingMeta);
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

		@Bean
		public OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}
	}
}
