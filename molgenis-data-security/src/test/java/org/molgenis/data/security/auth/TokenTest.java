package org.molgenis.data.security.auth;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {SecurityTestConfig.class})
class TokenTest extends AbstractSystemEntityTest {

  @Autowired TokenMetadata metadata;
  @Autowired TokenFactory factory;

  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, Token.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }

  private Token token;

  @BeforeEach
  void setup() {
    token = factory.create();
  }

  @Test
  void testIsExpiredNoExpirationDate() {
    assertFalse(token.isExpired());
  }

  @Test
  void testIsExpiredExpirationDateInFuture() {
    token.setExpirationDate(now().plus(1, MINUTES));
    assertFalse(token.isExpired());
  }

  @Test
  void testIsExpiredExpirationDateInPast() {
    token.setExpirationDate(now().minus(1, SECONDS));
    assertTrue(token.isExpired());
  }
}
