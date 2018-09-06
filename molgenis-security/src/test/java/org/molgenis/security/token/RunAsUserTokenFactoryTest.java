package org.molgenis.security.token;

import static org.mockito.Mockito.*;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RunAsUserTokenFactoryTest {
  private RunAsUserTokenFactory runAsUserTokenFactory;
  private UserDetailsChecker userDetailsChecker;

  @BeforeMethod
  public void setupBeforeMethod() {
    userDetailsChecker = mock(UserDetailsChecker.class);
    runAsUserTokenFactory = new RunAsUserTokenFactory(userDetailsChecker);
  }

  @Test
  public void testTokenForEnabledUser() {
    UserDetails userDetails = mock(UserDetails.class);
    runAsUserTokenFactory.create("test", userDetails, null);
    verify(userDetailsChecker).check(userDetails);
  }

  @Test(
      expectedExceptions = DisabledException.class,
      expectedExceptionsMessageRegExp = "User is disabled.")
  public void testTokenForDisabledUser() {
    UserDetails userDetails = mock(UserDetails.class);
    doThrow(new DisabledException("User is disabled.")).when(userDetailsChecker).check(userDetails);
    runAsUserTokenFactory.create("test", userDetails, null);
  }
}
