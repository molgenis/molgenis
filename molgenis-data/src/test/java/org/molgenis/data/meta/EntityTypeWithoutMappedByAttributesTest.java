package org.molgenis.data.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class EntityTypeWithoutMappedByAttributesTest extends AbstractMockitoTest {
  @Mock private EntityType entityType;
  private EntityTypeWithoutMappedByAttributes entityTypeWithoutMappedByAttributes;

  @BeforeEach
  void setUpBeforeEach() {
    entityTypeWithoutMappedByAttributes = new EntityTypeWithoutMappedByAttributes(entityType);
  }

  @Test
  void getOwnAttributeById() {
    String attributeIdentifier = "MyAttributeIdentifier";
    Attribute attribute = mock(Attribute.class);
    when(entityType.getOwnAttributeById(attributeIdentifier)).thenReturn(attribute);
    assertEquals(
        attribute, entityTypeWithoutMappedByAttributes.getOwnAttributeById(attributeIdentifier));
  }
}
