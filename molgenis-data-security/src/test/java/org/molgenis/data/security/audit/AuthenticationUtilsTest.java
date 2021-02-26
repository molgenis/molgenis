package org.molgenis.data.security.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.security.audit.AuthenticationUtils.getUsername;
import static org.molgenis.data.security.audit.AuthenticationUtils.isRunAsSystem;
import static org.molgenis.data.security.audit.AuthenticationUtils.isRunByUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = AuthenticationUtilsTest.Config.class)
class AuthenticationUtilsTest extends AbstractMockitoSpringContextTests {

  @Configuration
  static class Config {}

  private SecurityContext previousContext;

  @BeforeEach
  void beforeEach() {
    previousContext = SecurityContextHolder.getContext();
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  @WithMockUser("bofke")
  void testIsRunByUser() {
    assertTrue(isRunByUser());
  }

  @Test
  @WithMockSystemUser(originalUsername = "henk")
  void testIsRunByUserElevated() {
    assertTrue(isRunByUser());
  }

  @Test
  @WithMockSystemUser
  void testIsRunByUserSystem() {
    assertFalse(isRunByUser());
  }

  @Test
  @WithMockUser("bofke")
  void testIsRunAsSystemUser() {
    assertFalse(isRunAsSystem());
  }

  @Test
  @WithMockSystemUser(originalUsername = "henk")
  void testIsRunAsSystemElevated() {
    assertTrue(isRunAsSystem());
  }

  @Test
  @WithMockSystemUser
  void testIsRunAsSystemSystem() {
    assertFalse(isRunAsSystem());
  }

  @Test
  @WithMockUser("henk")
  void testGetUsername() {
    assertEquals("henk", getUsername());
  }

  @Test
  @WithMockSystemUser(originalUsername = "henk")
  void testGetUsernameElevated() {
    assertEquals("henk", getUsername());
  }

  @Test
  @WithMockSystemUser
  void testGetUsernameSystem() {
    assertEquals("SYSTEM", getUsername());
  }
}
