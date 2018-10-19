package org.molgenis.data.temp;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class NormalEntityTest {

  @Test
  public void getSetId() {
    NormalEntity ne = new NormalEntity();
    ne.setId("test");
    assertEquals(ne.getId(), "test");
  }
}
