package org.molgenis.metadata.manager.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.MREF;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.metadata.manager.mapper.AttributeMapper;
import org.molgenis.metadata.manager.mapper.EntityTypeMapper;
import org.molgenis.metadata.manager.mapper.PackageMapper;
import org.molgenis.metadata.manager.model.EditorAttribute;
import org.molgenis.metadata.manager.model.EditorAttributeResponse;
import org.molgenis.metadata.manager.model.EditorEntityType;
import org.molgenis.metadata.manager.model.EditorEntityTypeResponse;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {MetadataManagerServiceTest.Config.class})
class MetadataManagerServiceTest extends AbstractMockitoSpringContextTests {
  @Autowired private MetadataManagerService metadataManagerService;

  @Autowired private MetaDataService metaDataService;

  @Autowired private PackageMapper packageMapper;

  @Autowired private EntityTypeMapper entityTypeMapper;

  @Autowired private AttributeMapper attributeMapper;

  @Autowired private EntityTypeFactory entityTypeFactory;
  private final ArrayList<String> languageCodes =
      newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx");

  @Test
  void testGetEditorPackages() {
    Package package_ = mock(Package.class);
    List<Package> packages = newArrayList(package_);
    when(metaDataService.getPackages()).thenReturn(packages);

    EditorPackageIdentifier editorPackage = getEditorPackageIdentifier();
    when(packageMapper.toEditorPackage(package_)).thenReturn(editorPackage);

    List<EditorPackageIdentifier> actual = metadataManagerService.getEditorPackages();
    List<EditorPackageIdentifier> expected = newArrayList(editorPackage);

    assertEquals(actual, expected);
  }

  @Test
  void testGetEditorEntityType() {
    EntityType entityType = mock(EntityType.class);
    Attribute attr1 = mock(Attribute.class);
    Attribute attr2 = mock(Attribute.class);
    EditorEntityType editorEntityType = mock(EditorEntityType.class);

    when(metaDataService.getEntityType("id_1")).thenReturn(Optional.of(entityType));
    when(metaDataService.getReferringAttributes("id_1")).thenReturn(Stream.of(attr1, attr2));

    when(attr1.getDataType()).thenReturn(MREF);
    when(attr2.getDataType()).thenReturn(FILE);

    when(entityTypeMapper.toEditorEntityType(entityType, singletonList(attr2)))
        .thenReturn(editorEntityType);

    EditorEntityTypeResponse actual = metadataManagerService.getEditorEntityType("id_1");
    EditorEntityTypeResponse expected =
        EditorEntityTypeResponse.create(editorEntityType, languageCodes);

    assertEquals(actual, expected);
  }

  @Test
  void testGetNonExistingEditorEntityType() {
    Exception exception =
        assertThrows(
            UnknownEntityTypeException.class,
            () -> metadataManagerService.getEditorEntityType("unknownId"));
    assertThat(exception.getMessage()).containsPattern("id:unknownId");
  }

  @Test
  void testCreateEditorEntityType() {
    EditorEntityType editorEntityType = mock(EditorEntityType.class);
    when(entityTypeMapper.createEditorEntityType()).thenReturn(editorEntityType);

    EditorEntityTypeResponse actual = metadataManagerService.createEditorEntityType();
    EditorEntityTypeResponse expected =
        EditorEntityTypeResponse.create(editorEntityType, languageCodes);

    assertEquals(actual, expected);
  }

  @Test
  void testUpsertEntityType() {
    EditorEntityType editorEntityType = mock(EditorEntityType.class);
    EntityType entityType = mock(EntityType.class);

    when(entityTypeMapper.toEntityType(editorEntityType)).thenReturn(entityType);

    metadataManagerService.upsertEntityType(editorEntityType);
    verify(metaDataService, times(1)).upsertEntityTypes(newArrayList(entityType));
  }

  @Test
  void testCreateEditorAttribute() {
    EditorAttribute editorAttribute = mock(EditorAttribute.class);
    when(attributeMapper.createEditorAttribute()).thenReturn(editorAttribute);

    EditorAttributeResponse actual = metadataManagerService.createEditorAttribute();
    EditorAttributeResponse expected =
        EditorAttributeResponse.create(editorAttribute, languageCodes);

    assertEquals(actual, expected);
  }

  private EditorPackageIdentifier getEditorPackageIdentifier() {
    return EditorPackageIdentifier.create("test", "test");
  }

  @Configuration
  static class Config {
    @Bean
    MetaDataService metaDataService() {
      return mock(MetaDataService.class);
    }

    @Bean
    PackageMapper packageMapper() {
      return mock(PackageMapper.class);
    }

    @Bean
    EntityTypeMapper entityTypeMapper() {
      return mock(EntityTypeMapper.class);
    }

    @Bean
    AttributeMapper attributeMapper() {
      return mock(AttributeMapper.class);
    }

    @Bean
    EntityTypeFactory entityTypeFactory() {
      return mock(EntityTypeFactory.class);
    }

    @Bean
    MetadataManagerService metadataManagerService() {
      return new MetadataManagerServiceImpl(
          metaDataService(), packageMapper(), entityTypeMapper(), attributeMapper());
    }
  }
}
