package org.molgenis.semanticmapper.repository.impl;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState.CURATED;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.semanticmapper.config.MapperTestConfig;
import org.molgenis.semanticmapper.config.MappingConfig;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.meta.AttributeMappingMetadata;
import org.molgenis.semanticmapper.meta.EntityMappingMetadata;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {EntityMappingRepositoryImplTest.Config.class, MappingConfig.class})
public class EntityMappingRepositoryImplTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  @Autowired private AttributeMappingMetadata attrMappingMeta;

  @Autowired private EntityMappingMetadata entityMappingMeta;

  @Autowired private DataService dataService;

  @Autowired private EntityMappingRepositoryImpl entityMappingRepository;

  private static final String AUTO_ID = "1";

  @Test
  public void testToEntityMappings() {
    Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
    List<Attribute> sourceAttributes = Lists.newArrayList();
    EntityType sourceEntityType = entityTypeFactory.create("source");
    EntityType targetEntityType = entityTypeFactory.create("target");
    targetEntityType.addAttribute(targetAttribute);

    List<AttributeMapping> attributeMappings = Lists.newArrayList();
    attributeMappings.add(
        new AttributeMapping(
            "1", "targetAttribute", targetAttribute, "algorithm", sourceAttributes));

    List<EntityMapping> entityMappings =
        singletonList(
            new EntityMapping(AUTO_ID, sourceEntityType, targetEntityType, attributeMappings));

    Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
    attributeMappingEntity.set(EntityMappingMetadata.IDENTIFIER, AUTO_ID);
    attributeMappingEntity.set(AttributeMappingMetadata.TARGET_ATTRIBUTE, "targetAttribute");
    attributeMappingEntity.set(AttributeMappingMetadata.SOURCE_ATTRIBUTES, "sourceAttributes");
    attributeMappingEntity.set(AttributeMappingMetadata.ALGORITHM, "algorithm");

    List<Entity> attributeMappingEntities = Lists.newArrayList();
    attributeMappingEntities.add(attributeMappingEntity);

    List<Entity> entityMappingEntities = Lists.newArrayList();
    Entity entityMappingEntity = new DynamicEntity(entityMappingMeta);
    entityMappingEntity.set(EntityMappingMetadata.IDENTIFIER, AUTO_ID);
    entityMappingEntity.set(EntityMappingMetadata.TARGET_ENTITY_TYPE, "targetAttribute");
    entityMappingEntity.set(EntityMappingMetadata.ATTRIBUTE_MAPPINGS, attributeMappingEntities);

    entityMappingEntities.add(entityMappingEntity);

    when(dataService.getEntityType(
            entityMappingEntity.getString(EntityMappingMetadata.TARGET_ENTITY_TYPE)))
        .thenReturn(targetEntityType);
    when(dataService.getEntityType(
            entityMappingEntity.getString(EntityMappingMetadata.SOURCE_ENTITY_TYPE)))
        .thenReturn(sourceEntityType);

    assertEquals(entityMappingRepository.toEntityMappings(entityMappingEntities), entityMappings);
  }

  @Test
  public void testUpsert() {
    Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
    List<Attribute> sourceAttributes = Lists.newArrayList();
    EntityType sourceEntityType = entityTypeFactory.create("source");
    EntityType targetEntityType = entityTypeFactory.create("target");
    targetEntityType.addAttribute(targetAttribute);

    List<AttributeMapping> attributeMappings = Lists.newArrayList();
    attributeMappings.add(
        new AttributeMapping(
            "1",
            "targetAttribute",
            targetAttribute,
            "algorithm",
            sourceAttributes,
            CURATED.toString()));

    Collection<EntityMapping> entityMappings =
        singletonList(
            new EntityMapping(AUTO_ID, sourceEntityType, targetEntityType, attributeMappings));

    Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
    attributeMappingEntity.set(EntityMappingMetadata.IDENTIFIER, AUTO_ID);
    attributeMappingEntity.set(AttributeMappingMetadata.TARGET_ATTRIBUTE, "targetAttribute");
    attributeMappingEntity.set(AttributeMappingMetadata.SOURCE_ATTRIBUTES, "");
    attributeMappingEntity.set(AttributeMappingMetadata.ALGORITHM, "algorithm");
    attributeMappingEntity.set(AttributeMappingMetadata.ALGORITHM_STATE, CURATED.toString());

    List<Entity> attributeMappingEntities = Lists.newArrayList();
    attributeMappingEntities.add(attributeMappingEntity);

    List<Entity> entityMappingEntities = Lists.newArrayList();
    Entity entityMappingEntity = new DynamicEntity(entityMappingMeta);
    entityMappingEntity.set(EntityMappingMetadata.IDENTIFIER, AUTO_ID);
    entityMappingEntity.set(EntityMappingMetadata.SOURCE_ENTITY_TYPE, "source");
    entityMappingEntity.set(EntityMappingMetadata.TARGET_ENTITY_TYPE, "target");
    entityMappingEntity.set(EntityMappingMetadata.ATTRIBUTE_MAPPINGS, attributeMappingEntities);
    entityMappingEntities.add(entityMappingEntity);

    Assert.assertTrue(
        EntityUtils.equals(
            entityMappingRepository.upsert(entityMappings).get(0), entityMappingEntities.get(0)));
  }

  @Configuration
  @Import(MapperTestConfig.class)
  public static class Config {

    @Autowired private AttributeMappingMetadata attrMappingMeta;

    @Bean
    DataService dataService() {
      return Mockito.mock(DataService.class);
    }

    @Bean
    SemanticSearchService semanticSearchService() {
      return Mockito.mock(SemanticSearchService.class);
    }

    @Bean
    AttributeMappingRepositoryImpl attributeMappingRepository() {
      return new AttributeMappingRepositoryImpl(dataService(), attrMappingMeta);
    }

    @Bean
    EntityMappingRepositoryImpl entityMappingRepository() {
      return new EntityMappingRepositoryImpl(attributeMappingRepository());
    }

    @Bean
    UserService userService() {
      return Mockito.mock(UserService.class);
    }

    @Bean
    PermissionSystemService permissionSystemService() {
      return Mockito.mock(PermissionSystemService.class);
    }

    @Bean
    IdGenerator idGenerator() {
      return new IdGeneratorImpl();
    }

    @Bean
    EntityManager entityManager() {
      return Mockito.mock(EntityManager.class);
    }

    @Bean
    JsMagmaScriptEvaluator jsMagmaScriptEvaluator() {
      return Mockito.mock(JsMagmaScriptEvaluator.class);
    }

    @Bean
    public OntologyTagService ontologyTagService() {
      return Mockito.mock(OntologyTagService.class);
    }

    @Bean
    SystemPackageRegistry systemPackageRegistry() {
      return Mockito.mock(SystemPackageRegistry.class);
    }
  }
}
