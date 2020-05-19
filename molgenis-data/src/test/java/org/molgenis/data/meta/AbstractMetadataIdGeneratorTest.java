package org.molgenis.data.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    assertEquals("c6c784a6", abstractMetadataIdGenerator.generateHashcode(id));
  }

  @Test
  @Disabled
  public void demoSpeed() {
    String hash = "seed";
    Stopwatch stopwatch = Stopwatch.createStarted();
    for (var i = 0; i < 1000; i++) {
      hash = abstractMetadataIdGenerator.generateHashcode("attributeName" + hash);
    }
    var uncached = stopwatch.elapsed();

    stopwatch.reset().start();
    for (var i = 0; i < 1000; i++) {
      abstractMetadataIdGenerator.generateHashcode("attributeNameSame");
    }
    var cached = stopwatch.elapsed();

    System.out.println(uncached);
    System.out.println(cached);
  }
}
