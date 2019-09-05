package org.molgenis.semanticmapper.utils;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class UnitHelperTest {

  @Test
  public void superscriptToNumber() {
    assertEquals(UnitHelper.superscriptToNumber("⁵⁹⁸⁰³⁴test⁷¹⁶²²²"), "598034test716222");
  }

  @Test
  public void numberToSuperscript() {
    assertEquals(UnitHelper.numberToSuperscript("598034test716222"), "⁵⁹⁸⁰³⁴test⁷¹⁶²²²");
  }
}
