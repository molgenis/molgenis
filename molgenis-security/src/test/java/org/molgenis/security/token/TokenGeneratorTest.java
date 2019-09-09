package org.molgenis.security.token;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TokenGeneratorTest {

  @Test
  void generateToken() {
    TokenGenerator tg = new TokenGenerator();
    String token = tg.generateToken();
    assertNotNull(token);
    assertFalse(token.contains("-"));
    assertFalse(tg.generateToken().equals(token));
  }
}
