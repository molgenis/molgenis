package org.molgenis.security.twofactor;

import org.molgenis.security.login.MolgenisLoginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
	private static final String TWO_FACTOR_UNAUTHENTICATED_USER_ATTRIBUTE = "unAuthUserName";
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
	public String validateKeyAndAuthenticate(Model model, @RequestBody String key)
	{
		if(twoFactorAuthenticationService.isVerificationCodeValid(key)) {
			twoFactorAuthenticationService.authenticate();
		} else {
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No valid key found!");
		}
		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_INITIAL_URI)
	public String initial(Model model)
	{
		try
		{
			String userName = twoFactorAuthenticationService.getUnAuthenticatedUser();
			model.addAttribute(TWO_FACTOR_UNAUTHENTICATED_USER_ATTRIBUTE, userName);
		} catch (UsernameNotFoundException err) {
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No user found!");
		}

		model.addAttribute(TWO_FACTOR_IS_INITIAL_ATTRIBUTE, true);
		return MolgenisLoginController.VIEW_LOGIN;
	}


	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_SECRET_URI)
	public String setSecret(Model model, @RequestBody String secret)
	{

		twoFactorAuthenticationService.setSecretKey(secret);

		model.addAttribute(TWO_FACTOR_IS_ENABLED_ATTRIBUTE, true);
		return MolgenisLoginController.VIEW_LOGIN;
	}


}
