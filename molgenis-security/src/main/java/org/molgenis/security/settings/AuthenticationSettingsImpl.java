package org.molgenis.security.settings;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.MREF;

import java.util.List;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcClientMetadata;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSettingsImpl extends DefaultSettingsEntity
    implements AuthenticationSettings {
  private static final long serialVersionUID = 1L;

  private static final String ID = "auth";

  public AuthenticationSettingsImpl() {
    super(ID);
  }

  @Component
  public static class Meta extends DefaultSettingsEntityType {
    private static final String SIGNUP = "signup";
    private static final String SIGNUP_MODERATION = "signup_moderation";
    private static final String OIDC_CLIENTS = "oidcClients";
    private static final String SIGN_IN_2FA = "sign_in_2fa";

    private static final boolean DEFAULT_SIGNUP = false;
    private static final boolean DEFAULT_SIGNUP_MODERATION = true;

    private final OidcClientMetadata oidcClientMetadata;

    public Meta(OidcClientMetadata oidcClientMetadata) {
      super(ID);
      this.oidcClientMetadata = requireNonNull(oidcClientMetadata);
    }

    @Override
    public void init() {
      super.init();
      setLabel("Authentication settings");
      setDescription("Settings for authentication methods and user sign-up.");

      addAttribute(SIGNUP)
          .setDataType(BOOL)
          .setNillable(false)
          .setDefaultValue(String.valueOf(DEFAULT_SIGNUP))
          .setLabel("Allow users to sign up");
      addAttribute(SIGNUP_MODERATION)
          .setDataType(BOOL)
          .setNillable(false)
          .setDefaultValue(String.valueOf(DEFAULT_SIGNUP_MODERATION))
          .setLabel("Sign up moderation")
          .setDescription("Admins must accept sign up requests before account activation")
          .setVisibleExpression("$('" + SIGNUP + "').eq(true).value()");
      addAttribute(OIDC_CLIENTS)
          .setDataType(MREF)
          .setRefEntity(oidcClientMetadata)
          .setLabel("Authentication servers")
          .setDescription("OpenID Connect authentication servers")
          .setVisibleExpression(
              "$('"
                  + SIGNUP
                  + "').eq(true).value() && $('"
                  + SIGNUP_MODERATION
                  + "').eq(false).value()");
      addAttribute(SIGN_IN_2FA)
          .setDataType(ENUM)
          .setNillable(false)
          .setDefaultValue(TwoFactorAuthenticationSetting.DISABLED.getLabel())
          .setEnumOptions(
              asList(
                  TwoFactorAuthenticationSetting.DISABLED.getLabel(),
                  TwoFactorAuthenticationSetting.ENABLED.getLabel(),
                  TwoFactorAuthenticationSetting.ENFORCED.getLabel()))
          .setLabel("Two Factor Authentication")
          .setDescription("Enable or enforce users to sign in with Google Authenticator.")
          .setValidationExpression(getSignIn2FAValidationExpression());
    }

    /**
     * SIGN_IN_2FA == DISABLED || !SIGNUP || SIGNUP_MODERATION
     *
     * @return true if condition is met
     */
    private static String getSignIn2FAValidationExpression() {
      return String.format(
          "$('%s').eq('%s').or($('%s').not()).or($('%s')).value()",
          SIGN_IN_2FA,
          TwoFactorAuthenticationSetting.DISABLED.getLabel(),
          SIGNUP,
          SIGNUP_MODERATION);
    }
  }

  @Override
  public boolean getSignUp() {
    Boolean value = getBoolean(Meta.SIGNUP);
    return value != null ? value : false;
  }

  @Override
  public void setSignUp(boolean signUp) {
    set(Meta.SIGNUP, signUp);
  }

  @Override
  public boolean getSignUpModeration() {
    Boolean value = getBoolean(Meta.SIGNUP_MODERATION);
    return value != null ? value : false;
  }

  @Override
  public void setOidcClients(List<OidcClient> oidcClients) {
    set(Meta.OIDC_CLIENTS, oidcClients);
  }

  @Override
  public Iterable<OidcClient> getOidcClients() {
    return getEntities(Meta.OIDC_CLIENTS, OidcClient.class);
  }

  @Override
  public void setSignUpModeration(boolean signUpModeration) {
    set(Meta.SIGNUP_MODERATION, signUpModeration);
  }

  @Override
  public void setTwoFactorAuthentication(TwoFactorAuthenticationSetting twoFactorAuthentication) {
    set(Meta.SIGN_IN_2FA, twoFactorAuthentication.getLabel());
  }

  @Override
  public TwoFactorAuthenticationSetting getTwoFactorAuthentication() {
    return TwoFactorAuthenticationSetting.fromLabel(getString(Meta.SIGN_IN_2FA));
  }
}
