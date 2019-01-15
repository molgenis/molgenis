package org.molgenis.semanticmapper.repository.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState.CURATED;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetadata.ALGORITHM;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetadata.ALGORITHM_STATE;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetadata.IDENTIFIER;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetadata.SOURCE_ATTRIBUTES;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetadata.TARGET_ATTRIBUTE;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
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
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.ontology.core.config.OntologyConfig;
import org.molgenis.semanticmapper.config.MapperTestConfig;
import org.molgenis.semanticmapper.config.MappingConfig;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.meta.AttributeMappingMetadata;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      AttributeMappingRepositoryImplTest.Config.class,
      MappingConfig.class,
      OntologyConfig.class
    })
public class AttributeMappingRepositoryImplTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  @Autowired private AttributeMappingMetadata attrMappingMeta;

  @Autowired private AttributeMappingRepositoryImpl attributeMappingRepository;

  @Autowired private IdGenerator idGenerator;

  @Test
  public void testGetAttributeMappings() {
    Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
    List<Attribute> sourceAttributes = newArrayList();

    List<AttributeMapping> attributeMappings = newArrayList();
    attributeMappings.add(
        new AttributeMapping(
            "attributeMappingID",
            "targetAttribute",
            targetAttribute,
            "algorithm",
            sourceAttributes));

    Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
    attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
    attributeMappingEntity.set(TARGET_ATTRIBUTE, "targetAttribute");
    attributeMappingEntity.set(SOURCE_ATTRIBUTES, "sourceAttributes");
    attributeMappingEntity.set(ALGORITHM, "algorithm");

    List<Entity> attributeMappingEntities = newArrayList();
    attributeMappingEntities.add(attributeMappingEntity);

    EntityType sourceEntityType = entityTypeFactory.create("source");
    EntityType targetEntityType = entityTypeFactory.create("target");
    targetEntityType.addAttribute(targetAttribute);

    assertEquals(
        attributeMappingRepository.getAttributeMappings(
            attributeMappingEntities, sourceEntityType, targetEntityType),
        attributeMappings);
  }

  @Test
  public void testUpdate() {
    Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
    List<Attribute> sourceAttributes = newArrayList();

    targetAttribute.setDataType(STRING);

    Collection<AttributeMapping> attributeMappings =
        singletonList(
            new AttributeMapping(
                "attributeMappingID",
                "targetAttribute",
                targetAttribute,
                "algorithm",
                sourceAttributes,
                CURATED.toString()));

    List<Entity> result = newArrayList();
    Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
    attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
    attributeMappingEntity.set(TARGET_ATTRIBUTE, targetAttribute.getName());
    attributeMappingEntity.set(SOURCE_ATTRIBUTES, "");
    attributeMappingEntity.set(ALGORITHM, "algorithm");
    attributeMappingEntity.set(ALGORITHM_STATE, CURATED.toString());

    result.add(attributeMappingEntity);

    List<Entity> expectedAttrMappings = attributeMappingRepository.upsert(attributeMappings);
    assertEquals(expectedAttrMappings.size(), result.size());
    for (int i = 0; i < expectedAttrMappings.size(); ++i) {
      Assert.assertTrue(EntityUtils.equals(expectedAttrMappings.get(i), result.get(i)));
    }
  }

  @Test
  public void testInsert() {
    Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
    List<Attribute> sourceAttributes = newArrayList();
    targetAttribute.setDataType(STRING);

    Collection<AttributeMapping> attributeMappings =
        singletonList(
            new AttributeMapping(
                null,
                "targetAttribute",
                targetAttribute,
                "algorithm",
                sourceAttributes,
                CURATED.toString()));

    when(idGenerator.generateId()).thenReturn("attributeMappingID");

    List<Entity> result = newArrayList();
    Entity attributeMappingEntity = new DynamicEntity(attrMappingMeta);
    attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
    attributeMappingEntity.set(TARGET_ATTRIBUTE, targetAttribute.getName());
    attributeMappingEntity.set(SOURCE_ATTRIBUTES, "");
    attributeMappingEntity.set(ALGORITHM, "algorithm");
    attributeMappingEntity.set(ALGORITHM_STATE, CURATED.toString());

    result.add(attributeMappingEntity);

    List<Entity> expectedAttrMappings = attributeMappingRepository.upsert(attributeMappings);
    assertEquals(expectedAttrMappings.size(), result.size());
    for (int i = 0; i < expectedAttrMappings.size(); ++i) {
      Assert.assertTrue(EntityUtils.equals(expectedAttrMappings.get(i), result.get(i)));
    }
  }

  // regression test for https://github.com/molgenis/molgenis/issues/7831
  @Test
  public void testGetAttributeMappingsUnknownSourceAndTargetEntityType() {
    String identifier = "identifier";
    String targetAttribute = "targetAttribute";
    String algorithm = "algorithm";
    String algorithmState = "algorithmState";

    Entity attributeMappingEntity = mock(Entity.class);
    doReturn(identifier).when(attributeMappingEntity).getString("identifier");
    doReturn(targetAttribute).when(attributeMappingEntity).getString("targetAttribute");
    doReturn(algorithm).when(attributeMappingEntity).getString("algorithm");
    doReturn(algorithmState).when(attributeMappingEntity).getString("algorithmState");

    AttributeMapping attributeMapping =
        new AttributeMapping(
            identifier, targetAttribute, null, algorithm, emptyList(), algorithmState);
    assertEquals(
        attributeMappingRepository.getAttributeMappings(
            singletonList(attributeMappingEntity), null, null),
        singletonList(attributeMapping));
  }

  @Test
  public void testGetAlgorithmSourceAttributes() {
    Entity attributeMappingEntity = mock(Entity.class);
    String sourceAttributes = "attr0,attr1";
    when(attributeMappingEntity.getString("sourceAttributes")).thenReturn(sourceAttributes);
    EntityType sourceEntityType = mock(EntityType.class);
    Attribute attribute0 = mock(Attribute.class);
    Attribute attribute1 = mock(Attribute.class);
    doReturn(attribute0).when(sourceEntityType).getAttribute("attr0");
    doReturn(attribute1).when(sourceEntityType).getAttribute("attr1");
    assertEquals(
        attributeMappingRepository.getAlgorithmSourceAttributes(
            attributeMappingEntity, sourceEntityType),
        asList(attribute0, attribute1));
  }

  @Test
  public void testGetAlgorithmSourceAttributesNone() {
    Entity attributeMappingEntity = mock(Entity.class);
    EntityType sourceEntityType = mock(EntityType.class);
    assertEquals(
        attributeMappingRepository.getAlgorithmSourceAttributes(
            attributeMappingEntity, sourceEntityType),
        emptyList());
  }

  @Test
  public void testGetAlgorithmSourceAttributesUnknownAttribute() {
    Entity attributeMappingEntity = mock(Entity.class);
    String sourceAttributes = "unknownAttr,attr0";
    when(attributeMappingEntity.getString("sourceAttributes")).thenReturn(sourceAttributes);
    EntityType sourceEntityType = mock(EntityType.class);
    Attribute attribute0 = mock(Attribute.class);
    doReturn(null).when(sourceEntityType).getAttribute("unknownAttr");
    doReturn(attribute0).when(sourceEntityType).getAttribute("attr0");
    assertEquals(
        attributeMappingRepository.getAlgorithmSourceAttributes(
            attributeMappingEntity, sourceEntityType),
        singletonList(attribute0));
  }

  @Configuration
  @Import(MapperTestConfig.class)
  public static class Config {

    @Autowired private AttributeMappingMetadata attrMappingMeta;

    @Bean
    DataService dataService() {
      return mock(DataService.class);
    }

    @Bean
    SemanticSearchService semanticSearchService() {
      return mock(SemanticSearchService.class);
    }

    @Bean
    AttributeMappingRepositoryImpl attributeMappingRepository() {
      return new AttributeMappingRepositoryImpl(dataService(), attrMappingMeta);
    }

    @Bean
    UserService userService() {
      return mock(UserService.class);
    }

    @Bean
    PermissionSystemService permissionSystemService() {
      return mock(PermissionSystemService.class);
    }

    @Bean
    IdGenerator idGenerator() {
      return mock(IdGenerator.class);
    }

    @Bean
    EntityManager entityManager() {
      return mock(EntityManager.class);
    }

    @Bean
    JsMagmaScriptEvaluator jsMagmaScriptEvaluator() {
      return mock(JsMagmaScriptEvaluator.class);
    }

    @Bean
    public OntologyTagService ontologyTagService() {
      return mock(OntologyTagService.class);
    }

    @Bean
    SystemPackageRegistry systemPackageRegistry() {
      return mock(SystemPackageRegistry.class);
    }
  }
}
