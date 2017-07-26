package org.molgenis.security.twofactor;

import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/2fa")
public class TwoFactorAuthenticationController
{
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Autowired
	public TwoFactorAuthenticationController(TwoFactorAuthenticationService twoFactorAuthenticationService)
	{
		this.twoFactorAuthenticationService = twoFactorAuthenticationService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init2FA()
	{

		if(twoFactorAuthenticationService.is2FAEnabledForUser()) {

		} else {

		}


		return "view-2fa";
	}


	@RequestMapping(method = RequestMethod.GET)
	public String get2FAEnabled(@RequestParam String code)
	{
		if(twoFactorAuthenticationService.isVerificationCodeValid(code)) {
			twoFactorAuthenticationService.set2FAAuthenticated();
		}
		return "view-2fa";
	}
}
