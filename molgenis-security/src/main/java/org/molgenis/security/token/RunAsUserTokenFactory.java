package org.molgenis.security.token;

import static java.util.Objects.requireNonNull;

import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Component;

@Component
public class RunAsUserTokenFactory {
  private UserDetailsChecker userDetailsChecker;

  public RunAsUserTokenFactory(UserDetailsChecker userDetailsChecker) {
    this.userDetailsChecker = requireNonNull(userDetailsChecker);
  }

  public RunAsUserToken create(
      String key, UserDetails userDetails, Class<? extends Authentication> originalAuthentication) {
    userDetailsChecker.check(userDetails);
    return new RunAsUserToken(
        key,
        userDetails.getUsername(),
        userDetails.getPassword(),
        userDetails.getAuthorities(),
        originalAuthentication);
  }
}
