package org.molgenis.core.ui.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StyleTest {
  @Test
  void createLocal() {
    assertEquals("bootstrap", Style.createLocal("bootstrap.min.css").getName());
    assertEquals("yeti", Style.createLocal("bootstrap-yeti.min.css").getName());
    assertEquals("mystyle", Style.createLocal("mystyle.css").getName());
    assertEquals("my-style", Style.createLocal("my-style.css").getName());
  }
}
