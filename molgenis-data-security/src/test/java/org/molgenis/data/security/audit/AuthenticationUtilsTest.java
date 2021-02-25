package org.molgenis.data.security.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.security.audit.AuthenticationUtils.getUsername;
import static org.molgenis.data.security.audit.AuthenticationUtils.isRunAsSystem;
import static org.molgenis.data.security.audit.AuthenticationUtils.isRunByUser;
import static org.molgenis.data.security.audit.SecurityContextTestUtils.withElevatedUser;
import static org.molgenis.data.security.audit.SecurityContextTestUtils.withSystemToken;
import static org.molgenis.data.security.audit.SecurityContextTestUtils.withUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticationUtilsTest {

  private SecurityContext previousContext;

  @BeforeEach
  void beforeEach() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(testContext);
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void testIsRunByUser() {
    withUser("bofke");
    assertTrue(isRunByUser());
  }

  @Test
  void testIsRunByUserElevated() {
    withElevatedUser("henk");
    assertTrue(isRunByUser());
  }

  @Test
  void testIsRunByUserSystem() {
    withSystemToken();
    assertFalse(isRunByUser());
  }

  @Test
  void testIsRunAsSystemUser() {
    withUser("bofke");
    assertFalse(isRunAsSystem());
  }

  @Test
  void testIsRunAsSystemElevated() {
    withElevatedUser("henk");
    assertTrue(isRunAsSystem());
  }

  @Test
  void testIsRunAsSystemSystem() {
    withSystemToken();
    assertFalse(isRunAsSystem());
  }

  @Test
  void testGetUsername() {
    withUser("henk");
    assertEquals("henk", getUsername());
  }

  @Test
  void testGetUsernameElevated() {
    withElevatedUser("henk");
    assertEquals("henk", getUsername());
  }

  @Test
  void testGetUsernameSystem() {
    withSystemToken();
    assertEquals("SYSTEM", getUsername());
  }
}
