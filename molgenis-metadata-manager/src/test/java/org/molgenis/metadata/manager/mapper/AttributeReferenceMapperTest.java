package org.molgenis.metadata.manager.mapper;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.metadata.manager.model.EditorAttributeIdentifier.create;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.metadata.manager.model.EditorAttributeIdentifier;
import org.molgenis.metadata.manager.model.EditorEntityTypeIdentifier;

class AttributeReferenceMapperTest {
  @Mock private AttributeMetadata attributeMetadata;
  @Mock private DataService dataService;

  private AttributeReferenceMapper attributeReferenceMapper;

  @BeforeEach
  void setUpBeforeMethod() {
    MockitoAnnotations.initMocks(this);
    attributeReferenceMapper = new AttributeReferenceMapper(attributeMetadata, dataService);
  }

  @Test
  void testAttributeReferenceMapper() {
    assertThrows(NullPointerException.class, () -> new AttributeReferenceMapper(null, null));
  }

  @Test
  void testToEditorAttributeIdentifiers() {
    String id = "id";
    String label = "label";
    String entityTypeId = "id";
    String entityTypeLabel = "label";
    Attribute attribute = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    when(attribute.getIdentifier()).thenReturn(id);
    when(attribute.getLabel()).thenReturn(label);
    when(attribute.getEntity()).thenReturn(entityType);
    when(entityType.getId()).thenReturn(entityTypeId);
    when(entityType.getLabel()).thenReturn(entityTypeLabel);
    ImmutableList<EditorAttributeIdentifier> editorAttributeIdentifier =
        attributeReferenceMapper.toEditorAttributeIdentifiers(of(attribute));
    EditorEntityTypeIdentifier editorEntityTypeIdentifier =
        EditorEntityTypeIdentifier.create(entityTypeId, entityTypeLabel);
    assertEquals(of(create(id, label, editorEntityTypeIdentifier)), editorAttributeIdentifier);
  }

  @Test
  void testToEditorAttributeIdentifier() {
    String id = "id";
    String label = "label";
    String entityTypeId = "id";
    String entityTypeLabel = "label";
    Attribute attribute = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    when(attribute.getIdentifier()).thenReturn(id);
    when(attribute.getLabel()).thenReturn(label);
    when(attribute.getEntity()).thenReturn(entityType);
    when(entityType.getId()).thenReturn(entityTypeId);
    when(entityType.getLabel()).thenReturn(entityTypeLabel);
    EditorAttributeIdentifier editorAttributeIdentifier =
        attributeReferenceMapper.toEditorAttributeIdentifier(attribute);

    EditorEntityTypeIdentifier editorEntityTypeIdentifier =
        EditorEntityTypeIdentifier.create(entityTypeId, entityTypeLabel);
    assertEquals(create(id, label, editorEntityTypeIdentifier), editorAttributeIdentifier);
  }

  @Test
  void testToEditorAttributeIdentifierNull() {
    assertNull(attributeReferenceMapper.toEditorAttributeIdentifier(null));
  }

  @Test
  void testToAttributeReference() {
    String id = "id";
    String label = "label";
    EditorAttributeIdentifier editorAttributeIdentifier =
        EditorAttributeIdentifier.create(id, label);
    Attribute attribute = attributeReferenceMapper.toAttributeReference(editorAttributeIdentifier);
    assertEquals(id, attribute.getIdValue());
  }

  @Test
  void testToAttributeReferenceNull() {
    assertNull(attributeReferenceMapper.toAttributeReference(null));
  }
}
