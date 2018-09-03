package org.molgenis.security.token;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

public class TokenGeneratorTest {

  @Test
  public void generateToken() {
    TokenGenerator tg = new TokenGenerator();
    String token = tg.generateToken();
    assertNotNull(token);
    assertFalse(token.contains("-"));
    assertFalse(tg.generateToken().equals(token));
  }
}
