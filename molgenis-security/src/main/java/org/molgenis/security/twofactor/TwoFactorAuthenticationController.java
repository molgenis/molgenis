package org.molgenis.security.twofactor;

import org.molgenis.security.google.GoogleAuthenticatorService;
import org.molgenis.security.login.MolgenisLoginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/2fa")
public class TwoFactorAuthenticationController
{
	public static final String URI = "/2fa";
	public static final String TWO_FACTOR_ENABLED_URI = "/enabled";
	public static final String TWO_FACTOR_INITIAL_URI = "/initial";
	private static final String TWO_FACTOR_VALIDATION_URI = "/validate";
	private static final String TWO_FACTOR_SECRET_URI = "/secret";

	public static final String ATTRIBUTE_2FA_IS_INITIAL = "is2faInitial";
	public static final String ATTRIBUTE_2FA_IS_ENABLED = "is2faEnabled";
	public static final String ATTRIBUTE_2FA_SECRET_KEY = "secretKey";
	public static final String ATTRIBUTE_2FA_AUTHENTICATOR_URI = "authenticatorURI";
	public static final String ATTRIBUTE_HEADER_2FA_IS_INITIAL = "setup2faHeader";
	public static final String ATTRIBUTE_HEADER_2FA_VERIFY_CODE = "verifyKeyHeader";

	private static final String HEADER_VALUE_2FA_VERIFY_CODE = "Verification code";
	private static final String HEADER_VALUE_2FA_INITIAL = "Setup 2 factor authentication";

	private TwoFactorAuthenticationService twoFactorAuthenticationService;
	private OTPService otpService;
	private GoogleAuthenticatorService googleAuthenticatorService;

	@Autowired
	public TwoFactorAuthenticationController(TwoFactorAuthenticationService twoFactorAuthenticationService,
			OTPService otpService, GoogleAuthenticatorService googleAuthenticatorService)
	{
		this.twoFactorAuthenticationService = twoFactorAuthenticationService;
		this.otpService = otpService;
		this.googleAuthenticatorService = googleAuthenticatorService;
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_ENABLED_URI)
	public String enabled(Model model)
	{
		model.addAttribute(ATTRIBUTE_HEADER_2FA_VERIFY_CODE, HEADER_VALUE_2FA_VERIFY_CODE);
		model.addAttribute(ATTRIBUTE_2FA_IS_ENABLED, true);
		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_VALIDATION_URI)
	public String validateVerificationCodeAndAuthenticate(Model model, @RequestParam String verificationCode)
	{
		String redirectUri = "redirect:/";
		try
		{
			if (twoFactorAuthenticationService.isVerificationCodeValidForUser(verificationCode))
			{
				twoFactorAuthenticationService.authenticate();
			}
			else
			{
				setModelAttributesWhenNotValidated(model);
				redirectUri = MolgenisLoginController.VIEW_LOGIN;
			}
		}
		catch (Exception er)
		{
			setModelAttributesWhenNotValidated(model);
			redirectUri = MolgenisLoginController.VIEW_LOGIN;
		}

		return redirectUri;
	}

	private void setModelAttributesWhenNotValidated(Model model)
	{
		model.addAttribute(ATTRIBUTE_2FA_IS_ENABLED, true);
		model.addAttribute(ATTRIBUTE_HEADER_2FA_VERIFY_CODE, HEADER_VALUE_2FA_VERIFY_CODE);
		model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No valid verification code entered!");
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_INITIAL_URI)
	public String initial(Model model)
	{

		model.addAttribute(ATTRIBUTE_HEADER_2FA_IS_INITIAL, HEADER_VALUE_2FA_INITIAL);
		model.addAttribute(ATTRIBUTE_2FA_IS_INITIAL, true);

		try
		{
			String secretKey = twoFactorAuthenticationService.generateSecretKey();
			model.addAttribute(ATTRIBUTE_2FA_SECRET_KEY, secretKey);
			model.addAttribute(ATTRIBUTE_2FA_AUTHENTICATOR_URI,
					googleAuthenticatorService.getGoogleAuthenticatorURI(secretKey));
		}
		catch (UsernameNotFoundException err)
		{
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No user found!");
		}

		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_SECRET_URI)
	public String setSecret(Model model, @RequestParam String verificationCode, @RequestParam String secretKey)
	{
		String redirectUrl = "redirect:/";

		try
		{
			otpService.tryVerificationCode(verificationCode, secretKey);
			twoFactorAuthenticationService.setSecretKey(secretKey);
			twoFactorAuthenticationService.authenticate();
		}
		catch (Exception e)
		{
			model.addAttribute(ATTRIBUTE_2FA_IS_INITIAL, true);
			model.addAttribute(ATTRIBUTE_HEADER_2FA_IS_INITIAL, HEADER_VALUE_2FA_INITIAL);
			model.addAttribute(ATTRIBUTE_2FA_SECRET_KEY, secretKey);
			model.addAttribute(ATTRIBUTE_2FA_AUTHENTICATOR_URI,
					googleAuthenticatorService.getGoogleAuthenticatorURI(secretKey));
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No valid verification code entered!");
			redirectUrl = MolgenisLoginController.VIEW_LOGIN;
		}

		return redirectUrl;
	}

}
