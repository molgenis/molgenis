package org.molgenis.metadata.manager.mapper;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.metadata.manager.model.EditorEntityTypeIdentifier.create;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.metadata.manager.model.EditorEntityTypeIdentifier;

class EntityTypeReferenceMapperTest {
  @Mock private EntityTypeMetadata entityTypeMetadata;

  @Mock private DataService dataService;

  private EntityTypeReferenceMapper entityTypeReferenceMapper;

  @BeforeEach
  void setUpBeforeMethod() {
    MockitoAnnotations.initMocks(this);
    entityTypeReferenceMapper = new EntityTypeReferenceMapper(entityTypeMetadata, dataService);
  }

  @Test
  void testToEntityTypeReference() {
    String id = "id";
    EntityType entityType = entityTypeReferenceMapper.toEntityTypeReference(id);
    assertEquals(id, entityType.getIdValue());
  }

  @Test
  void testToEntityTypeReferenceNull() {
    assertNull(entityTypeReferenceMapper.toEntityTypeReference(null));
  }

  @Test
  void testToEditorEntityTypeIdentifiers() {
    String id = "id";
    String label = "label";
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(id);
    when(entityType.getLabel()).thenReturn(label);
    List<EditorEntityTypeIdentifier> editorEntityTypeIdentifiers =
        entityTypeReferenceMapper.toEditorEntityTypeIdentifiers(of(entityType));
    assertEquals(of(create(id, label)), editorEntityTypeIdentifiers);
  }

  @Test
  void testToEditorEntityTypeIdentifier() {
    String id = "id";
    String label = "label";
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(id);
    when(entityType.getLabel()).thenReturn(label);
    EditorEntityTypeIdentifier editorEntityTypeIdentifier =
        entityTypeReferenceMapper.toEditorEntityTypeIdentifier(entityType);
    assertEquals(create(id, label), editorEntityTypeIdentifier);
  }
}
