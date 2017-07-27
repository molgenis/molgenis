package org.molgenis.security.twofactor;

import org.molgenis.security.login.MolgenisLoginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
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

	private static final String TWO_FACTOR_IS_INITIAL_ATTRIBUTE = "is2faInitial";
	private static final String TWO_FACTOR_IS_ENABLED_ATTRIBUTE = "is2faEnabled";

	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Autowired
	public TwoFactorAuthenticationController(TwoFactorAuthenticationService twoFactorAuthenticationService)
	{
		this.twoFactorAuthenticationService = twoFactorAuthenticationService;
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_ENABLED_URI)
	public String enabled(Model model)
	{
		model.addAttribute(TWO_FACTOR_IS_ENABLED_ATTRIBUTE, true);
		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_VALIDATION_URI)
	public String validateVerificationCodeAndAuthenticate(Model model, @RequestBody String verificationCode)
	{
		if (twoFactorAuthenticationService.isVerificationCodeValid(verificationCode))
		{
			twoFactorAuthenticationService.authenticate();
		}
		else
		{
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No valid key found!");
		}
		return "redirect:/";
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_INITIAL_URI)
	public String initial(Model model)
	{
		try
		{
			String secretKey = twoFactorAuthenticationService.generateSecretKey();
			model.addAttribute("secretKey", secretKey);
			model.addAttribute("authenticatorURI", twoFactorAuthenticationService.getGoogleAuthenticatorURI(secretKey));
		}
		catch (UsernameNotFoundException err)
		{
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No user found!");
		}

		model.addAttribute(TWO_FACTOR_IS_INITIAL_ATTRIBUTE, true);
		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_SECRET_URI)
	public String setSecret(Model model, @RequestParam String verificationCode, @RequestParam String secretKey)
	{
		twoFactorAuthenticationService.tryVerificationCode(verificationCode, secretKey);
		twoFactorAuthenticationService.setSecretKey(secretKey);
		twoFactorAuthenticationService.authenticate();

		return "redirect:/";
	}

}
