package org.molgenis.security.settings;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.TEXT;

import java.util.List;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcClientMetadata;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSettingsImpl extends DefaultSettingsEntity
    implements AuthenticationSettings {

  private static final String ID = "auth";

  public AuthenticationSettingsImpl() {
    super(ID);
  }

  @Component
  public static class Meta extends DefaultSettingsEntityType {

    private static final String SIGNUP_FORM = "signup";
    private static final String SIGNUP_FORM_MODERATION = "signup_moderation";
    private static final String OIDC_CLIENTS = "oidcClients";
    private static final String SIGN_IN_2FA = "sign_in_2fa";
    private static final String PRIVACY_POLICY = "privacy_policy";
    private static final String PRIVACY_POLICY_LEVEL = "privacy_policy_level";
    private static final String PRIVACY_POLICY_CUSTOM_TEXT = "privacy_policy_custom_text";

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

      addAttribute(SIGNUP_FORM)
          .setDataType(BOOL)
          .setNillable(false)
          .setDefaultValue(String.valueOf(DEFAULT_SIGNUP))
          .setLabel("Show the sign-up form")
          .setDescription("If enabled, users will be able to sign up using the sign-up form");
      addAttribute(SIGNUP_FORM_MODERATION)
          .setDataType(BOOL)
          .setNullableExpression("false")
          .setDefaultValue(String.valueOf(DEFAULT_SIGNUP_MODERATION))
          .setLabel("Form sign-up moderation")
          .setDescription("Admins must approve users that sign up using the sign-up form")
          .setVisibleExpression(format("{%s}", SIGNUP_FORM));
      addAttribute(OIDC_CLIENTS)
          .setDataType(MREF)
          .setRefEntity(oidcClientMetadata)
          .setLabel("Authentication servers")
          .setDescription("OpenID Connect authentication servers");
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
          .setDescription(
              "Enable or enforce users to sign in with Google Authenticator. "
                  + "Must be disabled if you specify OpenID Connect authentication servers")
          .setValidationExpression(
              format(
                  "{%s} empty or {%s} = '%s'",
                  OIDC_CLIENTS, SIGN_IN_2FA, TwoFactorAuthenticationSetting.DISABLED.getLabel()));

      Attribute privacyPolicy =
          addAttribute(PRIVACY_POLICY)
              .setDataType(COMPOUND)
              .setDescription(
                  "The privacy policy is shown in the login dialog. Users need "
                      + "to agree to the policy before they can sign in.")
              .setLabel("Privacy Policy");
      addAttribute(PRIVACY_POLICY_LEVEL)
          .setDataType(ENUM)
          .setParent(privacyPolicy)
          .setNillable(false)
          .setDefaultValue(PrivacyPolicyLevel.CUSTOM.getLabel())
          .setEnumOptions(
              stream(PrivacyPolicyLevel.values()).map(PrivacyPolicyLevel::getLabel).toList())
          .setLabel("Privacy Policy Level");
      addAttribute(PRIVACY_POLICY_CUSTOM_TEXT)
          .setDataType(TEXT)
          .setParent(privacyPolicy)
          .setLabel("Privacy Policy Custom Text")
          .setDefaultValue(
              "I have read and agree to the privacy policy described at <a href=\\\"https://www.molgenis.org/MOLGENIS_Privacy_Policy.pdf\\\">https://www.molgenis.org/MOLGENIS_Privacy_Policy.pdf</a>\"")
          .setDescription("This text is shown when the \"Custom\" privacy policy level is chosen.")
          .setVisibleExpression(
              format("{%s} = '%s'", PRIVACY_POLICY_LEVEL, PrivacyPolicyLevel.CUSTOM.getLabel()));
    }
  }

  @Override
  public boolean getSignUpForm() {
    Boolean value = getBoolean(Meta.SIGNUP_FORM);
    return TRUE.equals(value);
  }

  @Override
  public void setSignUpForm(boolean signUp) {
    set(Meta.SIGNUP_FORM, signUp);
  }

  @Override
  public boolean getSignUpFormModeration() {
    Boolean value = getBoolean(Meta.SIGNUP_FORM_MODERATION);
    return TRUE.equals(value);
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
  public void setSignUpFormModeration(boolean signUpModeration) {
    set(Meta.SIGNUP_FORM_MODERATION, signUpModeration);
  }

  @Override
  public void setTwoFactorAuthentication(TwoFactorAuthenticationSetting twoFactorAuthentication) {
    set(Meta.SIGN_IN_2FA, twoFactorAuthentication.getLabel());
  }

  @Override
  public TwoFactorAuthenticationSetting getTwoFactorAuthentication() {
    return TwoFactorAuthenticationSetting.fromLabel(getString(Meta.SIGN_IN_2FA));
  }

  @Override
  public PrivacyPolicyLevel getPrivacyPolicyLevel() {
    return PrivacyPolicyLevel.fromLabel(getString(Meta.PRIVACY_POLICY_LEVEL));
  }

  @Override
  public String getPrivacyPolicyCustomText() {
    return getString(Meta.PRIVACY_POLICY_CUSTOM_TEXT);
  }
}
