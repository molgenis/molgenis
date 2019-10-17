package org.molgenis.semanticmapper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnitHelperTest {
  @Test
  void superscriptToNumber() {
    assertEquals("598034test716222", UnitHelper.superscriptToNumber("⁵⁹⁸⁰³⁴test⁷¹⁶²²²"));
  }

  @Test
  void numberToSuperscript() {
    assertEquals("⁵⁹⁸⁰³⁴test⁷¹⁶²²²", UnitHelper.numberToSuperscript("598034test716222"));
  }
}
