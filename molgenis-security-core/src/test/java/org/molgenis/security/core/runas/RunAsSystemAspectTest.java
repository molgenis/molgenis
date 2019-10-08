package org.molgenis.security.core.runas;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
    assertTrue(getAuthentication() instanceof UsernamePasswordAuthenticationToken);
    assertTrue(runAsSystem(this::getAuthentication) instanceof SystemSecurityToken);
    assertTrue(getAuthentication() instanceof UsernamePasswordAuthenticationToken);
  }

  private Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Configuration
  static class Config {}
}
