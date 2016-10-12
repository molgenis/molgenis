package org.molgenis.bbmri.directory.controller;

import com.google.gson.Gson;
import org.molgenis.bbmri.directory.model.Query;
import org.molgenis.bbmri.directory.settings.DirectorySettings;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.util.Objects.requireNonNull;
import static org.molgenis.bbmri.directory.controller.DirectoryController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@Controller
@RequestMapping(URI)
public class DirectoryController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryController.class);

	public static final String ID = "bbmridirectory";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String VIEW_DIRECTORY = "view-directory";

	private final MenuReaderService menuReaderService;

	private static final String API_URI = "/api/";

	@Autowired
	public DirectoryController(MenuReaderService menuReaderService)
	{
		super(URI);
		this.menuReaderService = menuReaderService;
	}

	@Autowired
	Gson gson;

	@Autowired
	DirectorySettings settings;

	@RequestMapping
	public String init(HttpServletRequest request, Model model)
	{
		model.addAttribute("username", getCurrentUsername());
		model.addAttribute("apiUrl", getApiUrl(request));
		model.addAttribute("baseUrl", getBaseUrl());
		return VIEW_DIRECTORY;
	}

	@RequestMapping("/query")
	@ResponseBody
	public String postQuery(@RequestBody Query query) throws Exception
	{
		LOG.info("Query " + query + " received, sending request");
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		headers.set("Authorization", this.generateBase64Authentication());
		HttpEntity entity = new HttpEntity(query, headers);

		LOG.trace("DirectorySettings.NEGOTIATOR_URL: [{}]", settings.getString(DirectorySettings.NEGOTIATOR_URL));

		String testUri = "https://www.google.com?q=" + query.getURL();
		return testUri;
		// return restTemplate.postForLocation(settings.getString(DirectorySettings.NEGOTIATOR_URL), entity);
	}

	/**
	 * Generate base64 authentication based on settings
	 *
	 * @return String
	 */
	private String generateBase64Authentication()
	{
		String username = settings.getString(DirectorySettings.USERNAME);
		String password = settings.getString(DirectorySettings.PASSWORD);
		requireNonNull(username, password);
		String userPass = username + password;
		String userPassBase64 = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
		return String.format("Base %s", userPassBase64);
	}

	private static String getApiUrl(HttpServletRequest request)
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
		return apiUrl;
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(DirectoryController.ID);
	}
}
