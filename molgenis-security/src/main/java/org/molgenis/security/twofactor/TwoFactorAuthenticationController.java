package org.molgenis.security.twofactor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/2fa")
public class TwoFactorAuthenticationController
{
	public static final String URI = "/2fa";
	public static final String TWO_FACTOR_ENABLED_URI = "/enabled";
	public static final String TWO_FACTOR_INITIAL_URI = "/initial";


	private static final String TWO_FACTOR_VALIDATION_URI = "/validate";
	private static final String TWO_FACTOR_SECRET_URI = "/secret";

	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Autowired
	public TwoFactorAuthenticationController(TwoFactorAuthenticationService twoFactorAuthenticationService)
	{
		this.twoFactorAuthenticationService = twoFactorAuthenticationService;
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_ENABLED_URI)
	public String enabled(Model model)
	{
		model.addAttribute("is2faEnabled", true);
		return "view-login";
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_VALIDATION_URI)
	public String validateKeyAndAuthenticate(@RequestBody String key)
	{
		if(twoFactorAuthenticationService.isVerificationCodeValid(key)) {
			twoFactorAuthenticationService.authenticate();
		}
		return "view-login";
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_INITIAL_URI)
	public String initial(Model model)
	{
		model.addAttribute("is2faInitial", true);
		return "view-2fa";
	}


	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_SECRET_URI)
	public String setSecret(@RequestBody String secret)
	{

		twoFactorAuthenticationService.setSecretKey(secret);

		Model model = new ExtendedModelMap();
		model.addAttribute("isInitial", true);
		return "view-2fa";
	}


}
