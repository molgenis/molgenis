package org.molgenis.security.twofactor;

import org.molgenis.security.google.GoogleAuthenticatorService;
import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping("/2fa")
public class TwoFactorAuthenticationController
{
	public static final String URI = "/2fa";
	public static final String TWO_FACTOR_CONFIGURED_URI = "/configured";
	public static final String TWO_FACTOR_INITIAL_URI = "/initial";
	private static final String TWO_FACTOR_VALIDATION_URI = "/validate";
	private static final String TWO_FACTOR_SECRET_URI = "/secret";
	private static final String TWO_FACTOR_RECOVER_URI = "/recover";

	public static final String ATTRIBUTE_2FA_IS_INITIAL = "is2faInitial";
	public static final String ATTRIBUTE_2FA_IS_CONFIGURED = "is2faConfigured";
	public static final String ATTRIBUTE_2FA_IS_RECOVER = "is2faRecover";

	public static final String ATTRIBUTE_2FA_SECRET_KEY = "secretKey";
	public static final String ATTRIBUTE_2FA_AUTHENTICATOR_URI = "authenticatorURI";
	public static final String ATTRIBUTE_2FA_HEADER = "twoFactorAuthenticatedHeader";

	private static final String HEADER_VALUE_2FA_IS_CONFIGURED = "Verification code";
	private static final String HEADER_VALUE_2FA_RECOVER = "Recovery code";
	private static final String HEADER_VALUE_2FA_IS_INITIAL = "Setup 2 factor authentication";

	private TwoFactorAuthenticationProvider authenticationProvider;
	private TwoFactorAuthenticationService twoFactorAuthenticationService;
	private RecoveryAuthenticationProvider recoveryAuthenticationProvider;
	private GoogleAuthenticatorService googleAuthenticatorService;

	@Autowired
	public TwoFactorAuthenticationController(TwoFactorAuthenticationProvider authenticationProvider,
			TwoFactorAuthenticationService twoFactorAuthenticationService,
			RecoveryAuthenticationProvider recoveryAuthenticationProvider,
			GoogleAuthenticatorService googleAuthenticatorService)
	{
		this.authenticationProvider = requireNonNull(authenticationProvider);
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.recoveryAuthenticationProvider = recoveryAuthenticationProvider;
		this.googleAuthenticatorService = requireNonNull(googleAuthenticatorService);
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_CONFIGURED_URI)
	public String configured(Model model)
	{
		model.addAttribute(ATTRIBUTE_2FA_HEADER, HEADER_VALUE_2FA_IS_CONFIGURED);
		model.addAttribute(ATTRIBUTE_2FA_IS_CONFIGURED, true);
		setModelAttributesRecoveryMode(model);
		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_VALIDATION_URI)
	public String validateVerificationCodeAndAuthenticate(Model model, @RequestParam String verificationCode)
	{
		String redirectUri = "redirect:/";
		try
		{
			TwoFactorAuthenticationToken authToken = new TwoFactorAuthenticationToken(verificationCode, null);
			Authentication authentication = authenticationProvider.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch (Exception err)
		{
			setModelAttributesWhenNotValidated(model, err);
			setModelAttributesRecoveryMode(model);
			redirectUri = MolgenisLoginController.VIEW_LOGIN;
		}

		return redirectUri;
	}

	private void setModelAttributesRecoveryMode(Model model)
	{
		try
		{
			twoFactorAuthenticationService.userIsBlocked();
		}
		catch (TooManyLoginAttemptsException err)
		{
			model.addAttribute(ATTRIBUTE_2FA_HEADER, HEADER_VALUE_2FA_RECOVER);
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, err.getMessage());
			model.addAttribute(ATTRIBUTE_2FA_IS_RECOVER, true);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_INITIAL_URI)
	public String initial(Model model)
	{

		model.addAttribute(ATTRIBUTE_2FA_HEADER, HEADER_VALUE_2FA_IS_INITIAL);
		model.addAttribute(ATTRIBUTE_2FA_IS_INITIAL, true);

		try
		{
			String secretKey = twoFactorAuthenticationService.generateSecretKey();
			model.addAttribute(ATTRIBUTE_2FA_SECRET_KEY, secretKey);
			model.addAttribute(ATTRIBUTE_2FA_AUTHENTICATOR_URI,
					googleAuthenticatorService.getGoogleAuthenticatorURI(secretKey));
		}
		catch (IllegalStateException err)
		{
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, determineErrorMessage(err));
		}

		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_SECRET_URI)
	public String setSecret(Model model, @RequestParam String verificationCode, @RequestParam String secretKey)
	{
		String redirectUrl = "redirect:/";

		try
		{
			TwoFactorAuthenticationToken authToken = new TwoFactorAuthenticationToken(verificationCode, secretKey);
			Authentication authentication = authenticationProvider.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch (Exception err)
		{
			model.addAttribute(ATTRIBUTE_2FA_IS_INITIAL, true);
			model.addAttribute(ATTRIBUTE_2FA_HEADER, HEADER_VALUE_2FA_IS_INITIAL);
			model.addAttribute(ATTRIBUTE_2FA_SECRET_KEY, secretKey);
			model.addAttribute(ATTRIBUTE_2FA_AUTHENTICATOR_URI,
					googleAuthenticatorService.getGoogleAuthenticatorURI(secretKey));
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, determineErrorMessage(err));
			redirectUrl = MolgenisLoginController.VIEW_LOGIN;
		}

		return redirectUrl;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_RECOVER_URI)
	public String recoverAccount(Model model, @RequestParam String recoveryCode)
	{
		String redirectUrl = "redirect:/";

		try
		{
			RecoveryAuthenticationToken authToken = new RecoveryAuthenticationToken(recoveryCode);
			Authentication authentication = recoveryAuthenticationProvider.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch (Exception e)
		{
			setModelAttributesWhenNotValidated(model, e);
			redirectUrl = MolgenisLoginController.VIEW_LOGIN;
		}

		return redirectUrl;
	}

	private void setModelAttributesWhenNotValidated(Model model, Exception err)
	{
		model.addAttribute(ATTRIBUTE_2FA_IS_CONFIGURED, true);
		model.addAttribute(ATTRIBUTE_2FA_HEADER, HEADER_VALUE_2FA_IS_CONFIGURED);
		model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, determineErrorMessage(err));
	}

	private String determineErrorMessage(Exception err)
	{
		String message = "Signin failed";
		if (err instanceof BadCredentialsException || err instanceof InvalidVerificationCodeException
				|| err instanceof TooManyLoginAttemptsException || err instanceof UsernameNotFoundException)
		{
			message = err.getMessage();
		}
		return message;
	}

}
