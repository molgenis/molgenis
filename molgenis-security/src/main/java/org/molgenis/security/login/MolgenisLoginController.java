package org.molgenis.security.login;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.account.AccountService;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/login")
public class MolgenisLoginController
{
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	public MolgenisLoginController(ResourceFingerprintRegistry resourceFingerprintRegistry)
	{
		if (resourceFingerprintRegistry == null) throw new IllegalArgumentException(
				"resourceFingerprintRegistry is null");
		this.resourceFingerprintRegistry = resourceFingerprintRegistry;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getLoginPage(Model model)
	{
		model.addAttribute("resource_fingerprint_registry", resourceFingerprintRegistry);
		model.addAttribute("enable_self_registration",
				molgenisSettings.getBooleanProperty(AccountService.KEY_PLUGIN_AUTH_ENABLE_SELFREGISTRATION, true));

		return "view-login";
	}

	@RequestMapping(method = RequestMethod.GET, params = "error")
	public String getLoginErrorPage(Model model)
	{
		model.addAttribute("resource_fingerprint_registry", resourceFingerprintRegistry);
		model.addAttribute("errorMessage", "The username or password you entered is incorrect.");
		model.addAttribute("enable_self_registration",
				molgenisSettings.getBooleanProperty(AccountService.KEY_PLUGIN_AUTH_ENABLE_SELFREGISTRATION, true));

		return "view-login";
	}
}
