package org.molgenis.semanticmapper.repository.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.semanticmapper.meta.MappingProjectMetadata.DEPTH;
import static org.molgenis.semanticmapper.meta.MappingProjectMetadata.IDENTIFIER;
import static org.molgenis.semanticmapper.meta.MappingProjectMetadata.MAPPING_PROJECT;
import static org.molgenis.semanticmapper.meta.MappingProjectMetadata.MAPPING_TARGETS;
import static org.molgenis.semanticmapper.meta.MappingProjectMetadata.NAME;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.semanticmapper.config.MapperTestConfig;
import org.molgenis.semanticmapper.mapping.model.MappingProject;
import org.molgenis.semanticmapper.mapping.model.MappingTarget;
import org.molgenis.semanticmapper.meta.MappingProjectMetadata;
import org.molgenis.semanticmapper.meta.MappingTargetMetadata;
import org.molgenis.semanticmapper.repository.MappingTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MappingProjectRepositoryImplTest.Config.class)
public class MappingProjectRepositoryImplTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  @Autowired private MappingProjectRepositoryImpl mappingProjectRepositoryImpl;

  @Autowired private DataService dataService;

  @Autowired private MappingTargetRepository mappingTargetRepository;

  @Autowired private IdGenerator idGenerator;

  @Autowired private MappingProjectMetadata mappingProjectMeta;

  @Autowired private MappingTargetMetadata mappingTargetMeta;

  private MappingTarget mappingTarget1;
  private MappingTarget mappingTarget2;
  private List<Entity> mappingTargetEntities;
  private MappingProject mappingProject;
  private Entity mappingProjectEntity;

  @BeforeMethod
  public void beforeMethod() {
    EntityType target1 = entityTypeFactory.create("target1");
    target1.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
    EntityType target2 = entityTypeFactory.create("target2");
    target2.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);

    mappingProject = new MappingProject("My first mapping project", 3);
    mappingTarget1 = mappingProject.addTarget(target1);
    mappingTarget2 = mappingProject.addTarget(target2);

    Entity mappingTargetEntity = new DynamicEntity(mappingTargetMeta);
    mappingTargetEntity.set(MappingTargetMetadata.TARGET, "target1");
    mappingTargetEntity.set(MappingTargetMetadata.IDENTIFIER, "mappingTargetID1");
    Entity mappingTargetEntity2 = new DynamicEntity(mappingTargetMeta);
    mappingTargetEntity2.set(MappingTargetMetadata.TARGET, "target2");
    mappingTargetEntity2.set(MappingTargetMetadata.IDENTIFIER, "mappingTargetID2");
    mappingTargetEntities = asList(mappingTargetEntity, mappingTargetEntity2);

    mappingProjectEntity = new DynamicEntity(mappingProjectMeta);
    mappingProjectEntity.set(IDENTIFIER, "mappingProjectID");
    mappingProjectEntity.set(MAPPING_TARGETS, mappingTargetEntities);
    mappingProjectEntity.set(NAME, "My first mapping project");
    mappingProjectEntity.set(DEPTH, 3);
  }

  @Test
  public void testAdd() {
    when(idGenerator.generateId()).thenReturn("mappingProjectID");
    when(mappingTargetRepository.upsert(asList(mappingTarget1, mappingTarget2)))
        .thenReturn(mappingTargetEntities);

    mappingProjectRepositoryImpl.add(mappingProject);

    ArgumentCaptor<DynamicEntity> argumentCaptor = ArgumentCaptor.forClass(DynamicEntity.class);
    Mockito.verify(dataService).add(eq(MAPPING_PROJECT), argumentCaptor.capture());
    assertEquals(argumentCaptor.getValue().getString(IDENTIFIER), "mappingProjectID");
    Assert.assertNull(mappingTarget1.getIdentifier());
    Assert.assertNull(mappingTarget2.getIdentifier());
  }

  @Test
  public void testAddWithIdentifier() {
    MappingProject mappingProject = new MappingProject("My first mapping project", 3);
    mappingProject.setIdentifier("mappingProjectID");
    try {
      mappingProjectRepositoryImpl.add(mappingProject);
    } catch (MolgenisDataException mde) {
      assertEquals(mde.getMessage(), "MappingProject already exists");
    }
  }

  @Test
  public void testDelete() {
    mappingProjectRepositoryImpl.delete("abc");
    Mockito.verify(dataService).deleteById(MAPPING_PROJECT, "abc");
  }

  @Test
  public void testQuery() {
    Query<Entity> q = new QueryImpl<>();
    when(dataService.findAll(MAPPING_PROJECT, q)).thenReturn(Stream.of(mappingProjectEntity));
    when(mappingTargetRepository.toMappingTargets(mappingTargetEntities))
        .thenReturn(asList(mappingTarget1, mappingTarget2));
    List<MappingProject> result = mappingProjectRepositoryImpl.getMappingProjects(q);
    mappingProject.setIdentifier("mappingProjectID");
    assertEquals(result, singletonList(mappingProject));
  }

  @Test
  public void testFindAll() {
    when(dataService.findAll(MAPPING_PROJECT)).thenReturn(Stream.of(mappingProjectEntity));
    when(mappingTargetRepository.toMappingTargets(mappingTargetEntities))
        .thenReturn(asList(mappingTarget1, mappingTarget2));
    List<MappingProject> result = mappingProjectRepositoryImpl.getAllMappingProjects();
    mappingProject.setIdentifier("mappingProjectID");
    assertEquals(result, singletonList(mappingProject));
  }

  @Test
  public void testUpdateUnknown() {
    mappingProject.setIdentifier("mappingProjectID");
    when(dataService.findOneById(TAG, "mappingProjectID")).thenReturn(null);
    try {
      mappingProjectRepositoryImpl.update(mappingProject);
      Assert.fail("Expected exception");
    } catch (MolgenisDataException expected) {
      assertEquals(expected.getMessage(), "MappingProject does not exist");
    }
  }

  @Configuration
  @Import(MapperTestConfig.class)
  public static class Config {
    @Autowired private DataService dataService;

    @Autowired private MappingProjectMetadata mappingProjectMeta;

    @Bean
    public MappingTargetRepository mappingTargetRepository() {
      return Mockito.mock(MappingTargetRepository.class);
    }

    @Bean
    public IdGenerator idGenerator() {
      return Mockito.mock(IdGenerator.class);
    }

    @Bean
    public MappingProjectRepositoryImpl mappingProjectRepositoryImpl() {
      return new MappingProjectRepositoryImpl(
          dataService, mappingTargetRepository(), idGenerator(), mappingProjectMeta);
    }
  }
}
