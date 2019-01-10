package org.molgenis.data.security.auth;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {SecurityTestConfig.class})
public class TokenTest extends AbstractSystemEntityTest {

  @Autowired TokenMetadata metadata;
  @Autowired TokenFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Token.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }

  private Token token;

  @BeforeMethod
  public void setup() {
    token = factory.create();
  }

  @Test
  public void testIsExpiredNoExpirationDate() {
    assertFalse(token.isExpired());
  }

  @Test
  public void testIsExpiredExpirationDateInFuture() {
    token.setExpirationDate(now().plus(1, MINUTES));
    assertFalse(token.isExpired());
  }

  @Test
  public void testIsExpiredExpirationDateInPast() {
    token.setExpirationDate(now().minus(1, SECONDS));
    assertTrue(token.isExpired());
  }
}
