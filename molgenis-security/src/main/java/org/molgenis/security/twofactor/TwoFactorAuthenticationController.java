package org.molgenis.security.twofactor;

import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationProvider;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationToken;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationProvider;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationToken;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.molgenis.security.twofactor.service.OtpService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping("/2fa")
public class TwoFactorAuthenticationController
{
	public static final String URI = "/2fa";
	public static final String TWO_FACTOR_CONFIGURED_URI = "/authenticate";
	public static final String TWO_FACTOR_ACTIVATION_URI = "/activation";
	public static final String ATTRIBUTE_2FA_RECOVER_MODE = "isRecoverMode";
	public static final String ATTRIBUTE_2FA_SECRET_KEY = "secretKey";
	public static final String ATTRIBUTE_2FA_AUTHENTICATOR_URI = "authenticatorURI";
	private static final String TWO_FACTOR_ACTIVATION_AUTHENTICATE_URI = TWO_FACTOR_ACTIVATION_URI + "/authenticate";
	private static final String TWO_FACTOR_VALIDATION_URI = "/validate";
	private static final String TWO_FACTOR_RECOVER_URI = "/recover";
	private static final String VIEW_2FA_ACTIVATION_MODAL = "view-2fa-activation-modal";
	private static final String VIEW_2FA_CONFIGURED_MODAL = "view-2fa-configured-modal";

	private final TwoFactorAuthenticationProvider authenticationProvider;
	private final TwoFactorAuthenticationService twoFactorAuthenticationService;
	private final RecoveryAuthenticationProvider recoveryAuthenticationProvider;
	private final OtpService otpService;

	public TwoFactorAuthenticationController(TwoFactorAuthenticationProvider authenticationProvider,
			TwoFactorAuthenticationService twoFactorAuthenticationService,
			RecoveryAuthenticationProvider recoveryAuthenticationProvider, OtpService otpService)
	{
		this.authenticationProvider = requireNonNull(authenticationProvider);
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.recoveryAuthenticationProvider = recoveryAuthenticationProvider;
		this.otpService = requireNonNull(otpService);
	}

	@GetMapping(TWO_FACTOR_CONFIGURED_URI)
	public String configured(Model model)
	{
		return VIEW_2FA_CONFIGURED_MODAL;
	}

	@PostMapping(TWO_FACTOR_VALIDATION_URI)
	public String validate(Model model, @RequestParam String verificationCode)
	{
		String redirectUri = "redirect:/";
		try
		{
			TwoFactorAuthenticationToken authToken = new TwoFactorAuthenticationToken(verificationCode, null);
			Authentication authentication = authenticationProvider.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch (AuthenticationException err)
		{
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, determineErrorMessage(err));
			redirectUri = VIEW_2FA_CONFIGURED_MODAL;
		}

		return redirectUri;
	}

	@GetMapping(TWO_FACTOR_ACTIVATION_URI)
	public String activation(Model model)
	{
		try
		{
			String secretKey = twoFactorAuthenticationService.generateSecretKey();
			model.addAttribute(ATTRIBUTE_2FA_SECRET_KEY, secretKey);
			model.addAttribute(ATTRIBUTE_2FA_AUTHENTICATOR_URI, otpService.getAuthenticatorURI(secretKey));
		}
		catch (IllegalStateException err)
		{
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, determineErrorMessage(err));
		}

		return VIEW_2FA_ACTIVATION_MODAL;
	}

	@PostMapping(TWO_FACTOR_ACTIVATION_AUTHENTICATE_URI)
	public String authenticate(Model model, @RequestParam String verificationCode, @RequestParam String secretKey)
	{
		String redirectUrl = "redirect:/menu/main/useraccount?showCodes=true#security";

		try
		{
			TwoFactorAuthenticationToken authToken = new TwoFactorAuthenticationToken(verificationCode, secretKey);
			Authentication authentication = authenticationProvider.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch (AuthenticationException err)
		{
			model.addAttribute(ATTRIBUTE_2FA_SECRET_KEY, secretKey);
			model.addAttribute(ATTRIBUTE_2FA_AUTHENTICATOR_URI, otpService.getAuthenticatorURI(secretKey));
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, determineErrorMessage(err));
			redirectUrl = VIEW_2FA_ACTIVATION_MODAL;
		}

		return redirectUrl;
	}

	@PostMapping(TWO_FACTOR_RECOVER_URI)
	public String recoverAccount(Model model, @RequestParam String recoveryCode)
	{
		String redirectUrl = "redirect:/";

		try
		{
			RecoveryAuthenticationToken authToken = new RecoveryAuthenticationToken(recoveryCode);
			Authentication authentication = recoveryAuthenticationProvider.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch (AuthenticationException e)
		{
			model.addAttribute(ATTRIBUTE_2FA_RECOVER_MODE, true);
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, determineErrorMessage(e));
			redirectUrl = VIEW_2FA_CONFIGURED_MODAL;
		}

		return redirectUrl;
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
