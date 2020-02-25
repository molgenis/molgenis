package org.molgenis.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class ExecutorServiceUtilsTest {

  @Test
  void testShutdownAndAwaitTermination() {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    assertDoesNotThrow(() -> ExecutorServiceUtils.shutdownAndAwaitTermination(executorService));
  }
}
