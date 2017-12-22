package org.molgenis.r;

import org.molgenis.security.token.TokenParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Returns the molgenis R api client script
 */
@Controller
public class MolgenisRController
{
	private static final String URI = "/molgenis.R";
	private static final String API_URI = "/api/";

	@GetMapping(URI)
	public String showMolgenisRApiClient(@TokenParam String token, HttpServletRequest request, Model model)
	{
		String apiUrl;
		if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
		{
			apiUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort() + API_URI;
		}
		else
		{
			apiUrl = request.getScheme() + "://" + request.getHeader("X-Forwarded-Host") + API_URI;
		}

		// If the request contains a molgenis security token, use it
		if (token != null)
		{
			model.addAttribute("token", token);
		}

		model.addAttribute("api_url", apiUrl);

		return "molgenis.R";
	}
}
