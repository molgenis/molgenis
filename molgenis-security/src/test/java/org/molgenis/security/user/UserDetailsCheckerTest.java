package org.molgenis.security.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;

class UserDetailsCheckerTest {
  @Test
  void check_enabledUser() {
    UserDetails userDetails = when(mock(UserDetails.class).isEnabled()).thenReturn(true).getMock();
    new MolgenisUserDetailsChecker().check(userDetails);
  }

  @Test
  void check_disabledUser() {
    UserDetails userDetails = when(mock(UserDetails.class).isEnabled()).thenReturn(false).getMock();
    assertThrows(
        DisabledException.class, () -> new MolgenisUserDetailsChecker().check(userDetails));
  }
}
