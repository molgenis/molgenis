package org.molgenis.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ResourceFingerprintRegistryTest {
  @Test
  void getFingerprint() throws IOException {
    assertEquals(
        "czpzLA", new ResourceFingerprintRegistry().getFingerprint(getClass(), "/resource.txt"));
  }
}
