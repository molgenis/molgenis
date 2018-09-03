package org.molgenis.data.importer.emx;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
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
}
