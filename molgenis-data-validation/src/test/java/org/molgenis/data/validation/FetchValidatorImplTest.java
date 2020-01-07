package org.molgenis.data.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class FetchValidatorTest extends AbstractMockitoTest {
  private FetchValidatorImpl fetchValidatorImpl;

  @BeforeEach
  void setUpBeforeEach() {
    fetchValidatorImpl = new FetchValidatorImpl();
  }

  @Test
  void validateFetch() {
    Fetch fetch = new Fetch().field("label").field("ref", new Fetch().field("refLabel"));
    EntityType entityType = mock(EntityType.class);
    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getName()).thenReturn("id");
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    Attribute refAttribute = mock(Attribute.class);
    EntityType refEntityType = mock(EntityType.class);
    Attribute refIdAttribute = mock(Attribute.class);
    when(refIdAttribute.getName()).thenReturn("refId");
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);
    when(refAttribute.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAttribute("ref")).thenReturn(refAttribute);
    Fetch expectedFetch =
        new Fetch(true)
            .field("id")
            .field("label")
            .field("ref", new Fetch(true).field("refId").field("refLabel"));
    assertEquals(expectedFetch, fetchValidatorImpl.validateFetch(fetch, entityType));
  }

  @Test
  void validateFetchNoValidationRequired() {
    Fetch fetch = new Fetch(true);
    EntityType entityType = mock(EntityType.class);
    fetchValidatorImpl.validateFetch(fetch, entityType);
    verifyNoInteractions(entityType);
  }
}
