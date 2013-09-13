package org.molgenis.security.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/login")
public class MolgenisLoginController
{
	// FIXME show login dialog
	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "forward:/";
	}
}
