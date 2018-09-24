package org.molgenis.security.settings;

import java.util.List;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;

public interface AuthenticationSettings {
  /** @return whether sign up is enabled */
  boolean getSignUp();

  /** @param signUp <code>true</code> if sign up is enabled */
  void setSignUp(boolean signUp);

  /** @return whether sign up is moderated */
  boolean getSignUpModeration();

  @SuppressWarnings("unused")
  void setOidcClients(List<OidcClient> oidcClients);

  Iterable<OidcClient> getOidcClients();

  /** @param signUpModeration <code>true</code> if sign up is moderated */
  void setSignUpModeration(boolean signUpModeration);

  /**
   * @param twoFactorAuthentication whether two factor authentication is disabled, enabled or
   *     enforced
   */
  void setTwoFactorAuthentication(TwoFactorAuthenticationSetting twoFactorAuthentication);

  /**
   * @return <code>enabled</code> or <code>enforced</code> if two factor authentication is optional
   *     or mandatory, <code>disabled</code> when it is off
   */
  TwoFactorAuthenticationSetting getTwoFactorAuthentication();
}
