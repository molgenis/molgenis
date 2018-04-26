package org.molgenis.security.settings;

import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;
import static org.molgenis.data.meta.AttributeType.*;

@Component
public class AuthenticationSettingsImpl extends DefaultSettingsEntity implements AuthenticationSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "auth";

	public AuthenticationSettingsImpl()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		private static final String SIGNUP = "signup";
		private static final String SIGNUP_MODERATION = "signup_moderation";
		private static final String GOOGLE_SIGN_IN = "google_sign_in";
		private static final String GOOGLE_APP_CLIENT_ID = "google_app_client_id";
		private static final String SIGN_IN_2FA = "sign_in_2fa";

		private static final boolean DEFAULT_SIGNUP = false;
		private static final boolean DEFAULT_SIGNUP_MODERATION = true;
		private static final boolean DEFAULT_GOOGLE_SIGN_IN = true;
		private static final String DEFAULT_GOOGLE_APP_CLIENT_ID = "130634143611-e2518d1uqu0qtec89pjgn50gbg95jin4.apps.googleusercontent.com";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Authentication settings");
			setDescription("Settings for authentication methods and user sign-up.");

			addAttribute(SIGNUP).setDataType(BOOL)
								.setNillable(false)
								.setDefaultValue(String.valueOf(DEFAULT_SIGNUP))
								.setLabel("Allow users to sign up");
			addAttribute(SIGNUP_MODERATION).setDataType(BOOL)
										   .setNillable(false)
										   .setDefaultValue(String.valueOf(DEFAULT_SIGNUP_MODERATION))
										   .setLabel("Sign up moderation")
										   .setDescription(
												   "Admins must accept sign up requests before account activation")
										   .setVisibleExpression("$('" + SIGNUP + "').eq(true).value()");
			addAttribute(GOOGLE_SIGN_IN).setDataType(BOOL)
										.setNillable(false)
										.setDefaultValue(String.valueOf(DEFAULT_GOOGLE_SIGN_IN))
										.setLabel("Enable Google Sign-In")
										.setDescription("Enable users to sign in with their existing Google account")
										.setVisibleExpression(
												"$('" + SIGNUP + "').eq(true).value() && $('" + SIGNUP_MODERATION
														+ "').eq(false).value()");
			addAttribute(GOOGLE_APP_CLIENT_ID).setDataType(STRING)
											  .setNillable(false)
											  .setDefaultValue(DEFAULT_GOOGLE_APP_CLIENT_ID)
											  .setLabel("Google app client ID")
											  .setDescription("Google app client ID used during Google Sign-In")
											  .setVisibleExpression("$('" + GOOGLE_SIGN_IN + "').eq(true).value()");
			addAttribute(SIGN_IN_2FA).setDataType(ENUM)
									 .setNillable(false)
									 .setDefaultValue(TwoFactorAuthenticationSetting.DISABLED.getLabel())
									 .setEnumOptions(asList(TwoFactorAuthenticationSetting.DISABLED.getLabel(),
											 TwoFactorAuthenticationSetting.ENABLED.getLabel(),
											 TwoFactorAuthenticationSetting.ENFORCED.getLabel()))
									 .setLabel("Two Factor Authentication")
									 .setDescription(
											 "Enable or enforce users to sign in with Google Authenticator. Can not be used when Google Sign-In is enabled.")
									 .setValidationExpression(getSignIn2FAValidationExpression());
		}

		/**
		 * SIGN_IN_2FA == DISABLED || !SIGNUP || SIGNUP_MODERATION || !GOOGLE_SIGN_IN
		 *
		 * @return true if condition is met
		 */
		private static String getSignIn2FAValidationExpression()
		{
			return String.format("$('%s').eq('%s').or($('%s').not()).or($('%s')).or($('%s').not()).value()",
					SIGN_IN_2FA, TwoFactorAuthenticationSetting.DISABLED.getLabel(), SIGNUP, SIGNUP_MODERATION,
					GOOGLE_SIGN_IN);
		}
	}

	@Override
	public boolean getSignUp()
	{
		Boolean value = getBoolean(Meta.SIGNUP);
		return value != null ? value : false;
	}

	@Override
	public void setSignUp(boolean signUp)
	{
		set(Meta.SIGNUP, signUp);
	}

	@Override
	public boolean getSignUpModeration()
	{
		Boolean value = getBoolean(Meta.SIGNUP_MODERATION);
		return value != null ? value : false;
	}

	@Override
	public void setSignUpModeration(boolean signUpModeration)
	{
		set(Meta.SIGNUP_MODERATION, signUpModeration);
	}

	@Override
	public void setGoogleSignIn(boolean googleSignIn)
	{
		set(Meta.GOOGLE_SIGN_IN, googleSignIn);
	}

	@Override
	public boolean getGoogleSignIn()
	{
		Boolean value = getBoolean(Meta.GOOGLE_SIGN_IN);
		return value != null ? value : false;
	}

	@Override
	public void setGoogleAppClientId(String googleAppClientId)
	{
		set(Meta.GOOGLE_APP_CLIENT_ID, googleAppClientId);
	}

	@Override
	public String getGoogleAppClientId()
	{
		return getString(Meta.GOOGLE_APP_CLIENT_ID);
	}

	@Override
	public void setTwoFactorAuthentication(TwoFactorAuthenticationSetting twoFactorAuthentication)
	{
		set(Meta.SIGN_IN_2FA, twoFactorAuthentication.getLabel());
	}

	@Override
	public TwoFactorAuthenticationSetting getTwoFactorAuthentication()
	{
		return TwoFactorAuthenticationSetting.fromLabel(getString(Meta.SIGN_IN_2FA));
	}
}
