package org.molgenis.data.mapper.repository.impl;

import com.google.common.collect.Lists;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.mapper.config.MapperTestConfig;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.mapper.meta.EntityMappingMetaData;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.UserService;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState.CURATED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { EntityMappingRepositoryImplTest.Config.class, MappingConfig.class })
public class EntityMappingRepositoryImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

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
		Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
		List<Attribute> sourceAttributes = Lists.newArrayList();
		EntityType sourceEntityType = entityTypeFactory.create("source");
		EntityType targetEntityType = entityTypeFactory.create("target");
		targetEntityType.addAttribute(targetAttribute);

		List<AttributeMapping> attributeMappings = Lists.newArrayList();
		attributeMappings.add(new AttributeMapping("1", targetAttribute, "algorithm", sourceAttributes));

		List<EntityMapping> entityMappings = singletonList(
				new EntityMapping(AUTO_ID, sourceEntityType, targetEntityType, attributeMappings));

		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		attributeMappingEntity.set(AttributeMappingMetaData.TARGET_ATTRIBUTE, "targetAttribute");
		attributeMappingEntity.set(AttributeMappingMetaData.SOURCE_ATTRIBUTES, "sourceAttributes");
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");

		List<Entity> attributeMappingEntities = Lists.newArrayList();
		attributeMappingEntities.add(attributeMappingEntity);

		List<Entity> entityMappingEntities = Lists.newArrayList();
		Entity entityMappingEntity = new DynamicEntity(entityMappingMeta);
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		entityMappingEntity.set(EntityMappingMetaData.TARGET_ENTITY_TYPE, "targetAttribute");
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTE_MAPPINGS, attributeMappingEntities);

		entityMappingEntities.add(entityMappingEntity);

		when(dataService.getEntityType(
				entityMappingEntity.getString(EntityMappingMetaData.TARGET_ENTITY_TYPE))).thenReturn(targetEntityType);
		when(dataService.getEntityType(
				entityMappingEntity.getString(EntityMappingMetaData.SOURCE_ENTITY_TYPE))).thenReturn(sourceEntityType);

		assertEquals(entityMappingRepository.toEntityMappings(entityMappingEntities), entityMappings);
	}

	@Test
	public void testUpsert()
	{
		Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
		List<Attribute> sourceAttributes = Lists.newArrayList();
		EntityType sourceEntityType = entityTypeFactory.create("source");
		EntityType targetEntityType = entityTypeFactory.create("target");
		targetEntityType.addAttribute(targetAttribute);

		List<AttributeMapping> attributeMappings = Lists.newArrayList();
		attributeMappings.add(
				new AttributeMapping("1", targetAttribute, "algorithm", sourceAttributes, CURATED.toString()));

		Collection<EntityMapping> entityMappings = singletonList(
				new EntityMapping(AUTO_ID, sourceEntityType, targetEntityType, attributeMappings));

		Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
		attributeMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		attributeMappingEntity.set(AttributeMappingMetaData.TARGET_ATTRIBUTE, "targetAttribute");
		attributeMappingEntity.set(AttributeMappingMetaData.SOURCE_ATTRIBUTES, "");
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM_STATE, CURATED.toString());

		List<Entity> attributeMappingEntities = Lists.newArrayList();
		attributeMappingEntities.add(attributeMappingEntity);

		List<Entity> entityMappingEntities = Lists.newArrayList();
		Entity entityMappingEntity = new DynamicEntity(entityMappingMeta);
		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
		entityMappingEntity.set(EntityMappingMetaData.SOURCE_ENTITY_TYPE, "source");
		entityMappingEntity.set(EntityMappingMetaData.TARGET_ENTITY_TYPE, "target");
		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTE_MAPPINGS, attributeMappingEntities);
		entityMappingEntities.add(entityMappingEntity);

		assertTrue(EntityUtils.equals(entityMappingRepository.upsert(entityMappings).get(0),
				entityMappingEntities.get(0)));
	}

	@Configuration
	@Import(MapperTestConfig.class)
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
		UserService userService()
		{
			return mock(UserService.class);
		}

		@Bean
		PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			return new IdGeneratorImpl();
		}

		@Bean
		EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}

		@Bean
		JsMagmaScriptEvaluator jsMagmaScriptEvaluator()
		{
			return mock(JsMagmaScriptEvaluator.class);
		}

		@Bean
		public OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		DefaultPackage defaultPackage()
		{
			return mock(DefaultPackage.class);
		}

		@Bean
		SystemPackageRegistry systemPackageRegistry()
		{
			return mock(SystemPackageRegistry.class);
		}
	}
}
