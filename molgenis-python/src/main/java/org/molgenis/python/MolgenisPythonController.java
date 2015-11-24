package org.molgenis.python;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.security.token.TokenExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Returns the molgenis Python api client script
 */
@Controller
public class MolgenisPythonController
{
	private static final String URI = "/molgenis.py";
	private static final String API_URI = "/api/";

	@RequestMapping(method = RequestMethod.GET, value = URI)
	public String showMolgenisPythonApiClient(HttpServletRequest request, Model model)
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
		String token = TokenExtractor.getToken(request);
		if (token != null)
		{
			model.addAttribute("token", token);
		}

		model.addAttribute("api_url", apiUrl);

		return "molgenis.py";
	}
}
