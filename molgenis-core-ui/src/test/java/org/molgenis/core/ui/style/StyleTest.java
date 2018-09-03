package org.molgenis.core.ui.style;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class StyleTest {
  @Test
  public void createLocal() {
    assertEquals(Style.createLocal("bootstrap.min.css").getName(), "bootstrap");
    assertEquals(Style.createLocal("bootstrap-yeti.min.css").getName(), "yeti");
    assertEquals(Style.createLocal("mystyle.css").getName(), "mystyle");
    assertEquals(Style.createLocal("my-style.css").getName(), "my-style");
  }
}
