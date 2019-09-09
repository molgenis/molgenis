package org.molgenis.data.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractMetadataIdGeneratorTest {
  private AbstractMetadataIdGenerator abstractMetadataIdGenerator;

  @BeforeEach
  void setUpBeforeMethod() {
    abstractMetadataIdGenerator = mock(AbstractMetadataIdGenerator.class, CALLS_REAL_METHODS);
  }

  @Test
  void testGenerateHashcode() {
    String id = "0123456789";
    assertEquals(abstractMetadataIdGenerator.generateHashcode(id), "c6c784a6");
  }
}
