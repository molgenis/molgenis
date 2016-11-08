package org.molgenis.bbmri.directory.controller;

import com.google.gson.Gson;
import org.molgenis.bbmri.directory.model.NegotiatorQuery;
import org.molgenis.bbmri.directory.settings.DirectorySettings;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.rsql.MolgenisRSQL;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.google.api.client.util.Maps.newHashMap;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.bbmri.directory.controller.DirectoryController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@Controller
@RequestMapping(URI + "/**")
public class DirectoryController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryController.class);

	public static final String ID = "bbmridirectory";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String VIEW_DIRECTORY = "view-directory";

	private final MenuReaderService menuReaderService;

	private static final String API_URI = "/api/";

	private final Gson gson;
	private final DirectorySettings settings;
	private final MolgenisRSQL molgenisRSQL;
	private final MetaDataService metaDataService;

	@Autowired
	public DirectoryController(MenuReaderService menuReaderService, DirectorySettings settings,
			MolgenisRSQL molgenisRSQL, Gson gson, MetaDataService metaDataService)
	{
		super(URI);
		this.menuReaderService = menuReaderService;
		this.settings = settings;
		this.molgenisRSQL = molgenisRSQL;
		this.gson = gson;
		this.metaDataService = metaDataService;
	}

	@RequestMapping()
	public String init(@RequestParam(required = false) String q, @RequestParam(required = false) String nToken,
			HttpServletRequest request, Model model)
	{
		if (q != null)
		{
			LOG.info("Request received with an rsql query\n\n" + q + "\n\nsetting filter state");
			Query<Entity> query = molgenisRSQL
					.createQuery(q, metaDataService.getEntityType("eu_bbmri_eric_collections"));

			List<QueryRule> rules = query.getRules().get(0).getNestedRules();
			Map<String, Object> filters = newHashMap();
			for (QueryRule rule : rules)
			{
				String field = rule.getField();
				// For the demo, we only parse the boolean fields
				if (field != null)
				{
					filters.put(field, singletonList(rule.getValue()));
				}
			}

			// Use a hard coded mref for demo effect
			String materials = "[{operator : 'AND',value : [{id:'PLASMA',label:'Plasma'}, {id:'TISSUE_FROZEN',label:'Cryo tissue'}]},'OR',{value : { id : 'NAV', label : 'Not available' }}]";
			filters.put("materials", gson.fromJson(materials, List.class));
			model.addAttribute("filters", gson.toJson(filters));

			LOG.trace("Generated filters from RSQL:\n" + gson.toJson(filters));
		}

		if (nToken != null)
		{
			LOG.info("Token received [%s", nToken);
			model.addAttribute("nToken", nToken);
		}

		model.addAttribute("username", getCurrentUsername());
		model.addAttribute("apiUrl", getApiUrl(request));
		model.addAttribute("baseUrl", getBaseUrl());
		return VIEW_DIRECTORY;
	}

	@RequestMapping(value = "/query", produces = "application/json")
	@ResponseBody
	public String postQuery(@RequestBody NegotiatorQuery query) throws Exception
	{
		LOG.info("NegotiatorQuery\n\n" + query + "\n\nreceived, sending request");
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		headers.set("Authorization", generateBase64Authentication());
		HttpEntity entity = new HttpEntity(query, headers);

		LOG.trace("DirectorySettings.NEGOTIATOR_URL: [{}]", settings.getString(DirectorySettings.NEGOTIATOR_URL));
		String redirectURL = restTemplate.postForLocation(settings.getString(DirectorySettings.NEGOTIATOR_URL), entity)
				.toASCIIString();

		LOG.trace("Redirecting to " + redirectURL);
		return redirectURL;
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
		String userPass = username + ":" + password;
		String userPassBase64 = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
		return String.format("Basic %s", userPassBase64);
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
