package org.molgenis.bbmri.directory.controller;

import com.google.gson.Gson;
import org.molgenis.bbmri.directory.model.Collection;
import org.molgenis.bbmri.directory.model.Query;
import org.molgenis.bbmri.directory.settings.DirectorySettings;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.bbmri.directory.controller.DirectoryController.URI;
import static org.molgenis.bbmri.directory.model.Collection.createCollection;
import static org.molgenis.bbmri.directory.model.Query.createQuery;

@Controller
@RequestMapping(URI)
public class DirectoryController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryController.class);

	public static final String ID = "bbmridirectory";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String VIEW_DIRECTORY = "view-directory";
	public DirectoryController()
	{
		super(URI);
	}

	@Autowired
	Gson gson;

	@Autowired
	DirectorySettings settings;

	@RequestMapping
	public String init(Model model)
	{
		return VIEW_DIRECTORY;
	}

	@RequestMapping("/query")
	public java.net.URI postQuery() throws Exception
	{
		LOG.debug("Query received, sending request");

		// TODO create structured data

		Collection collection = createCollection("eric:collectionID:AT_MUG:collection:all_samples",
				"bbmri-eric:biobankID:AT_MUG");

		String humanReadable = "name:Biobank Graz";
		List<Collection> collections = singletonList(collection);

		String url = "http://molgenis.todo.nl";

		Query query = createQuery(url, collections, humanReadable, null);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		headers.set("Authorization", this.generateBase64Authentication());
		HttpEntity entity = new HttpEntity(query, headers);

		LOG.trace("DirectorySettings.NEGOTIATOR_URL: [{}]", settings.getString(DirectorySettings.NEGOTIATOR_URL));
		return restTemplate.postForLocation(settings.getString(DirectorySettings.NEGOTIATOR_URL), entity);
	}

	/**
	 * Generate base64 authentication based on settings
	 *
	 * @return String
	 */
	private String generateBase64Authentication(){
		String username = settings.getString(DirectorySettings.USERNAME);
		String password = settings.getString(DirectorySettings.PASSWORD);
		requireNonNull(username, password);
		String userPass = username + password;
		String userPassBase64 = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
		return String.format("Base %s", userPassBase64);
	}
}
