package org.molgenis.security.settings;

import java.util.List;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;

public interface AuthenticationSettings {

  /** @return whether the sign-up form is enabled */
  boolean getSignUpForm();

  /** @param signUp <code>true</code> if the sign-up form is enabled */
  void setSignUpForm(boolean signUp);

  /** @return whether signing up via the form is moderated */
  boolean getSignUpFormModeration();

  /** @param signUpModeration <code>true</code> if sign-up via the form is moderated */
  void setSignUpFormModeration(boolean signUpModeration);

  @SuppressWarnings("unused")
  void setOidcClients(List<OidcClient> oidcClients);

  Iterable<OidcClient> getOidcClients();

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

  /** @return the selected privacy policy level message that will be shown at login */
  PrivacyPolicyLevel getPrivacyPolicyLevel();

  /** @return the custom privacy policy message */
  String getPrivacyPolicyCustomText();
}
