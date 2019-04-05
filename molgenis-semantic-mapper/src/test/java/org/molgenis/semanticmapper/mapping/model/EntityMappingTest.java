package org.molgenis.semanticmapper.mapping.model;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityMappingTest extends AbstractMockitoTest {
  @Mock private EntityType sourceEntityType;
  @Mock private EntityType targetEntityType;
  private EntityMapping entityMapping;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityMapping = new EntityMapping(sourceEntityType, targetEntityType);
  }

  @Test
  public void testAddAttributeMapping() {
    AttributeMapping attributeMapping = mock(AttributeMapping.class);
    String targetAttributeName = "MyTargetAttributeName";
    when(attributeMapping.getTargetAttributeName()).thenReturn(targetAttributeName);
    entityMapping.addAttributeMapping(attributeMapping);
    assertEquals(entityMapping.getAttributeMappings(), singletonList(attributeMapping));
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testAddAttributeMappingAlreadyExists() {
    AttributeMapping attributeMapping = mock(AttributeMapping.class);
    String targetAttributeName = "MyTargetAttributeName";
    when(attributeMapping.getTargetAttributeName()).thenReturn(targetAttributeName);
    entityMapping.addAttributeMapping(attributeMapping);
    entityMapping.addAttributeMapping(attributeMapping);
  }
}
