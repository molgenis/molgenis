package org.molgenis.security.core.runas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class RunAsSystemTokenTest {

  @Test
  public void testRunAsSystemAsSystem() {
    var systemSecurityToken = mock(SystemSecurityToken.class);
    assertThrows(IllegalStateException.class, () -> {
      new RunAsSystemToken(systemSecurityToken);
    });
  }

  @Test
  public void testRunAsSystemAsRunAsSystem() {
    var runAsSystemToken = mock(RunAsSystemToken.class);
    assertThrows(IllegalStateException.class, () -> {
      new RunAsSystemToken(runAsSystemToken);
    });
  }

  @Test
  public void testRunAsUser() {
    var userToken = mock(UsernamePasswordAuthenticationToken.class);
    assertDoesNotThrow(() -> {
      new RunAsSystemToken(userToken);
    });
  }

  @Test
  public void testRunAsNull() {
    assertThrows(NullPointerException.class, () -> {
      new RunAsSystemToken(null);
    });
  }
}