package org.molgenis.security.core.runas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.WithMockSystemUser;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = RunAsSystemAspectTest.Config.class)
class RunAsSystemAspectTest extends AbstractMockitoSpringContextTests {

  private static SecurityContext previousContext;

  @BeforeAll
  static void setUpBeforeClass() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(testContext);
  }

  @AfterAll
  static void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  @WithMockUser
  void testRunAsSystemRunnableAsSystem() {
    var originalAuth = getAuthentication();
    var auth = (SystemSecurityToken) runAsSystem(this::getAuthentication);

    assertTrue(originalAuth instanceof UsernamePasswordAuthenticationToken);
    assertTrue(auth.getOriginalAuthentication().isPresent());
    assertEquals(originalAuth, auth.getOriginalAuthentication().get());
    assertTrue(getAuthentication() instanceof UsernamePasswordAuthenticationToken);
  }

  @Test
  @WithMockSystemUser
  void testRunAsSystemWithSystemToken() {
    var originalAuth = getAuthentication();

    assertTrue(originalAuth instanceof SystemSecurityToken);
    assertSame(originalAuth, runAsSystem(this::getAuthentication));
    assertSame(originalAuth, getAuthentication());
  }

  @Test
  @WithMockSystemUser(originalUsername = "henk")
  void testRunAsSystemWithElevatedUser() {
    var originalAuth = getAuthentication();

    assertTrue(originalAuth instanceof SystemSecurityToken);
    assertSame(originalAuth, runAsSystem(this::getAuthentication));
    assertSame(originalAuth, getAuthentication());
  }

  private Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Configuration
  static class Config {}
}
