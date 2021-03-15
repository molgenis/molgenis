package org.molgenis.data.security.auth;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.security.auth.VOGroupRoleMembership.Status.CURRENT;
import static org.molgenis.data.security.auth.VOGroupRoleMembership.Status.FUTURE;
import static org.molgenis.data.security.auth.VOGroupRoleMembership.Status.PAST;

import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {SecurityTestConfig.class})
class VOGroupRoleMembershipTest extends AbstractSystemEntityTest {

  @Autowired VOGroupRoleMembershipMetadata metadata;
  @Autowired VOGroupRoleMembershipFactory factory;

  @Test
  @SuppressWarnings({"java:S5786", "java:S2699"})
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata,
        VOGroupRoleMembership.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }

  @Test
  void currentMembershipEmptyTo() {
    var membership = factory.create("test");
    membership.setFrom(now());
    assertTrue(membership.isCurrent());
    assertEquals(CURRENT, membership.getStatus());
  }

  @Test
  void currentMembershipFutureTo() {
    var membership = factory.create("test");
    membership.setFrom(now());
    membership.setTo(now().plusSeconds(20));
    assertTrue(membership.isCurrent());
    assertEquals(CURRENT, membership.getStatus());
  }

  @Test
  void pastMembership() {
    var membership = factory.create("test");
    membership.setFrom(now());
    membership.setTo(now());
    assertFalse(membership.isCurrent());
    assertEquals(PAST, membership.getStatus());
  }

  @Test
  void futureMembership() {
    var membership = factory.create("test");
    membership.setFrom(now().plusSeconds(20));
    assertFalse(membership.isCurrent());
    assertEquals(FUTURE, membership.getStatus());
  }
}
