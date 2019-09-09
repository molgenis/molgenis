package org.molgenis.core.ui.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StyleTest {
  @Test
  void createLocal() {
    assertEquals(Style.createLocal("bootstrap.min.css").getName(), "bootstrap");
    assertEquals(Style.createLocal("bootstrap-yeti.min.css").getName(), "yeti");
    assertEquals(Style.createLocal("mystyle.css").getName(), "mystyle");
    assertEquals(Style.createLocal("my-style.css").getName(), "my-style");
  }
}
