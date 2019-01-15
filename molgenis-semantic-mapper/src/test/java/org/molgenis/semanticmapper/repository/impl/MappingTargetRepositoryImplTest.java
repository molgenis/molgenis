package org.molgenis.semanticmapper.repository.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.molgenis.semanticmapper.meta.MappingTargetMetadata.ENTITY_MAPPINGS;
import static org.molgenis.semanticmapper.meta.MappingTargetMetadata.IDENTIFIER;
import static org.molgenis.semanticmapper.meta.MappingTargetMetadata.TARGET;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.semanticmapper.config.MapperTestConfig;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.mapping.model.MappingTarget;
import org.molgenis.semanticmapper.meta.EntityMappingMetadata;
import org.molgenis.semanticmapper.meta.MappingTargetMetadata;
import org.molgenis.semanticmapper.repository.EntityMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for the MappingTargetRepository. Tests the MappingTargetRepository in isolation. */
@ContextConfiguration(classes = {MappingTargetRepositoryImplTest.Config.class})
public class MappingTargetRepositoryImplTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  @Autowired private DataService dataService;

  @Autowired private EntityMappingRepository entityMappingRepository;

  @Autowired private MappingTargetRepositoryImpl mappingTargetRepository;

  @Autowired private EntityMappingMetadata entityMappingMeta;

  @Autowired private MappingTargetMetadata mappingTargetMeta;

  @Autowired private IdGenerator idGenerator;

  private List<MappingTarget> mappingTargets;

  private List<Entity> mappingTargetEntities;

  private EntityType targetEntityType;

  private List<Entity> entityMappingEntities;

  private List<EntityMapping> entityMappings;

  @Captor ArgumentCaptor<Collection<EntityMapping>> entityMappingCaptor;

  @BeforeMethod
  public void beforeMethod() {
    // POJOs
    EntityType sourceEntityType = entityTypeFactory.create("source");
    targetEntityType = entityTypeFactory.create("target");
    Attribute targetAttribute = attrMetaFactory.create().setName("targetAttribute");
    targetEntityType.addAttribute(targetAttribute);
    entityMappings =
        singletonList(
            new EntityMapping("entityMappingID", sourceEntityType, targetEntityType, emptyList()));
    mappingTargets =
        singletonList(new MappingTarget("mappingTargetID", targetEntityType, entityMappings));

    // Entities
    Entity entityMappingEntity = new DynamicEntity(entityMappingMeta);
    entityMappingEntity.set(EntityMappingMetadata.IDENTIFIER, "entityMappingID");
    entityMappingEntity.set(EntityMappingMetadata.SOURCE_ENTITY_TYPE, "source");
    entityMappingEntity.set(EntityMappingMetadata.TARGET_ENTITY_TYPE, "target");
    entityMappingEntity.set(EntityMappingMetadata.ATTRIBUTE_MAPPINGS, emptyList());
    Entity mappingTargetEntity = new DynamicEntity(mappingTargetMeta);
    mappingTargetEntity.set(IDENTIFIER, "mappingTargetID");
    mappingTargetEntity.set(TARGET, "target");

    entityMappingEntities = singletonList(entityMappingEntity);
    mappingTargetEntity.set(ENTITY_MAPPINGS, entityMappingEntities);

    mappingTargetEntities = singletonList(mappingTargetEntity);
  }

  @Test
  public void testToMappingTargets() {
    when(dataService.getEntityType("target")).thenReturn(targetEntityType);
    when(entityMappingRepository.toEntityMappings(entityMappingEntities))
        .thenReturn(entityMappings);
    when(dataService.hasRepository("target")).thenReturn(true);

    assertEquals(mappingTargetRepository.toMappingTargets(mappingTargetEntities), mappingTargets);
  }

  @Test
  public void testUpdate() {
    when(entityMappingRepository.upsert(entityMappings)).thenReturn(entityMappingEntities);
    List<Entity> result = mappingTargetRepository.upsert(mappingTargets);

    assertEquals(mappingTargetEntities.size(), result.size());
    for (int i = 0; i < mappingTargetEntities.size(); ++i) {
      Assert.assertTrue(EntityUtils.equals(mappingTargetEntities.get(i), result.get(i)));
    }
  }

  @Test
  public void testInsert() {
    mappingTargets.get(0).setIdentifier(null);

    when(idGenerator.generateId()).thenReturn("mappingTargetID");
    when(entityMappingRepository.upsert(entityMappings)).thenReturn(entityMappingEntities);
    List<Entity> result = mappingTargetRepository.upsert(mappingTargets);

    assertEquals(mappingTargetEntities.size(), result.size());
    for (int i = 0; i < mappingTargetEntities.size(); ++i) {
      Assert.assertTrue(EntityUtils.equals(mappingTargetEntities.get(i), result.get(i)));
    }
  }

  @Configuration
  @Import(MapperTestConfig.class)
  public static class Config {
    @Bean
    DataService dataService() {
      return Mockito.mock(DataService.class);
    }

    @Bean
    EntityMappingRepository entityMappingRepository() {
      return Mockito.mock(EntityMappingRepository.class);
    }

    @Bean
    MappingTargetRepositoryImpl mappingTargetRepository() {
      return new MappingTargetRepositoryImpl(entityMappingRepository());
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
      return Mockito.mock(IdGenerator.class);
    }
  }
}
