package org.molgenis.core.util;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import org.testng.annotations.Test;

public class ResourceFingerprintRegistryTest {
  @Test
  public void getFingerprint() throws IOException {
    assertEquals(
        new ResourceFingerprintRegistry().getFingerprint(getClass(), "/resource.txt"), "czpzLA");
  }
}
