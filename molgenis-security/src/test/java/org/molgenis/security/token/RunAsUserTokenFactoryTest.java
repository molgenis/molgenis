package org.molgenis.security.token;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

class RunAsUserTokenFactoryTest {
  private RunAsUserTokenFactory runAsUserTokenFactory;
  private UserDetailsChecker userDetailsChecker;

  @BeforeEach
  void setupBeforeMethod() {
    userDetailsChecker = mock(UserDetailsChecker.class);
    runAsUserTokenFactory = new RunAsUserTokenFactory(userDetailsChecker);
  }

  @Test
  void testTokenForEnabledUser() {
    UserDetails userDetails = mock(UserDetails.class);
    runAsUserTokenFactory.create("test", userDetails, null);
    verify(userDetailsChecker).check(userDetails);
  }

  @Test
  void testTokenForDisabledUser() {
    UserDetails userDetails = mock(UserDetails.class);
    doThrow(new DisabledException("User is disabled.")).when(userDetailsChecker).check(userDetails);
    assertThrows(
        DisabledException.class, () -> runAsUserTokenFactory.create("test", userDetails, null));
  }
}
