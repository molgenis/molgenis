package org.molgenis.security.login;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/login")
public class MolgenisLoginController
{
	@RequestMapping(method = RequestMethod.GET)
	public String getLoginPage()
	{
		return "view-login";
	}

	@RequestMapping(method = RequestMethod.GET, params = "error")
	public String getLoginErrorPage(Model model)
	{
		model.addAttribute("errorMessage", "The username or password you entered is incorrect.");
		return "view-login";
	}
}
