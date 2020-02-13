package org.molgenis.data.elasticsearch.generator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;

/** See {@link QueryGeneratorIT} */
class QueryGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;

  @Test
  void testQueryGenerator() {
    assertDoesNotThrow(() -> new QueryGenerator(documentIdGenerator));
  }

  @Test
  void testQueryGeneratorNullPointerException() {
    assertThrows(NullPointerException.class, () -> new QueryGenerator(null));
  }
}
