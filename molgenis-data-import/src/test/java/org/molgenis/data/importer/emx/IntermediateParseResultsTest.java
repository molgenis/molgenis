package org.molgenis.data.importer.emx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.test.AbstractMockitoTest;

class IntermediateParseResultsTest extends AbstractMockitoTest {
  private IntermediateParseResults intermediateParseResults;
  @Mock private EntityTypeFactory entityTypeFactory;

  @BeforeEach
  void setUpBeforeMethod() {
    intermediateParseResults = new IntermediateParseResults(entityTypeFactory);
  }

  @Test
  void testAddEntityType() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.setLabel(any())).thenReturn(entityType);
    when(entityType.setPackage(any())).thenReturn(entityType);

    when(entityTypeFactory.create("entityType")).thenReturn(entityType);

    assertEquals(intermediateParseResults.addEntityType("entityType"), entityType);
    verify(entityType).setLabel("entityType");
  }

  @Test
  void testSetDefaultLookupAttributes() {
    Attribute label = mock(Attribute.class);
    when(label.getName()).thenReturn("label");
    Attribute id = mock(Attribute.class);
    when(id.getName()).thenReturn("id");
    when(id.isVisible()).thenReturn(true);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getOwnLabelAttribute()).thenReturn(label);
    when(entityType.getOwnIdAttribute()).thenReturn(id);
    intermediateParseResults.setDefaultLookupAttributes(entityType, 9);
    verify(id).setLookupAttributeIndex(9);
    verify(label).setLookupAttributeIndex(10);
  }

  @Test
  void testSetDefaultLookupAttributesIdEqualsLabel() {
    Attribute label = mock(Attribute.class);
    when(label.getName()).thenReturn("id");
    Attribute id = mock(Attribute.class);
    when(id.getName()).thenReturn("id");
    when(id.isVisible()).thenReturn(true);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getOwnLabelAttribute()).thenReturn(label);
    when(entityType.getOwnIdAttribute()).thenReturn(id);
    intermediateParseResults.setDefaultLookupAttributes(entityType, 9);
    verify(id).setLookupAttributeIndex(9);
    verify(label, times(0)).setLookupAttributeIndex(any());
  }

  @Test
  void testSetDefaultLookupAttributesInvisibleId() {
    Attribute label = mock(Attribute.class);
    when(label.getName()).thenReturn("label");
    Attribute id = mock(Attribute.class);
    when(id.getName()).thenReturn("id");
    when(id.isVisible()).thenReturn(false);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getOwnLabelAttribute()).thenReturn(label);
    when(entityType.getOwnIdAttribute()).thenReturn(id);
    IntermediateParseResults.setDefaultLookupAttributes(entityType, 9);
    verify(label).setLookupAttributeIndex(9);
    verify(id, times(0)).setLookupAttributeIndex(any());
  }
}
