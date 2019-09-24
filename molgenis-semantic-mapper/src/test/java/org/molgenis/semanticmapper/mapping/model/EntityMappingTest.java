package org.molgenis.semanticmapper.mapping.model;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class EntityMappingTest extends AbstractMockitoTest {
  @Mock private EntityType sourceEntityType;
  @Mock private EntityType targetEntityType;
  private EntityMapping entityMapping;

  @BeforeEach
  void setUpBeforeMethod() {
    entityMapping = new EntityMapping(sourceEntityType, targetEntityType);
  }

  @Test
  void testAddAttributeMapping() {
    AttributeMapping attributeMapping = mock(AttributeMapping.class);
    String targetAttributeName = "MyTargetAttributeName";
    when(attributeMapping.getTargetAttributeName()).thenReturn(targetAttributeName);
    entityMapping.addAttributeMapping(attributeMapping);
    assertEquals(
        singletonList(attributeMapping), new ArrayList<>(entityMapping.getAttributeMappings()));
  }

  @Test
  void testAddAttributeMappingAlreadyExists() {
    AttributeMapping attributeMapping = mock(AttributeMapping.class);
    String targetAttributeName = "MyTargetAttributeName";
    when(attributeMapping.getTargetAttributeName()).thenReturn(targetAttributeName);
    entityMapping.addAttributeMapping(attributeMapping);
    assertThrows(
        IllegalStateException.class, () -> entityMapping.addAttributeMapping(attributeMapping));
  }
}
