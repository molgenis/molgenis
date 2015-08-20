package org.molgenis.security.login;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.data.settings.AppSettings;
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
	private final AppSettings appSettings;

	@Autowired
	public MolgenisLoginController(ResourceFingerprintRegistry resourceFingerprintRegistry, AppSettings appSettings)
	{
		this.resourceFingerprintRegistry = checkNotNull(resourceFingerprintRegistry);
		this.appSettings = checkNotNull(appSettings);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getLoginPage(Model model)
	{
		model.addAttribute("resource_fingerprint_registry", resourceFingerprintRegistry);
		model.addAttribute("enable_self_registration", appSettings.getSignUp());
		return "view-login";
	}

	@RequestMapping(method = RequestMethod.GET, params = "error")
	public String getLoginErrorPage(Model model)
	{
		model.addAttribute("resource_fingerprint_registry", resourceFingerprintRegistry);
		model.addAttribute("errorMessage", "The username or password you entered is incorrect.");
		model.addAttribute("enable_self_registration", appSettings.getSignUp());
		return "view-login";
	}
}
