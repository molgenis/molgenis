package org.molgenis.data.importer.emx;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntermediateParseResultsTest extends AbstractMockitoTest {
  private IntermediateParseResults intermediateParseResults;
  @Mock private EntityTypeFactory entityTypeFactory;

  @BeforeMethod
  public void setUpBeforeMethod() {
    intermediateParseResults = new IntermediateParseResults(entityTypeFactory);
  }

  @Test
  public void testAddEntityType() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.setLabel(any())).thenReturn(entityType);
    when(entityType.setPackage(any())).thenReturn(entityType);

    when(entityTypeFactory.create("entityType")).thenReturn(entityType);

    assertEquals(intermediateParseResults.addEntityType("entityType"), entityType);
    verify(entityType).setLabel("entityType");
  }

  @Test
  public void testSetDefaultLookupAttributes() {
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
  public void testSetDefaultLookupAttributesIdEqualsLabel() {
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
  public void testSetDefaultLookupAttributesInvisibleId() {
    Attribute label = mock(Attribute.class);
    when(label.getName()).thenReturn("label");
    Attribute id = mock(Attribute.class);
    when(id.getName()).thenReturn("id");
    when(id.isVisible()).thenReturn(false);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getOwnLabelAttribute()).thenReturn(label);
    when(entityType.getOwnIdAttribute()).thenReturn(id);
    intermediateParseResults.setDefaultLookupAttributes(entityType, 9);
    verify(label).setLookupAttributeIndex(9);
    verify(id, times(0)).setLookupAttributeIndex(any());
  }
}
