package org.molgenis.bbmri.directory.controller;

import com.google.gson.Gson;
import org.molgenis.bbmri.directory.model.Collection;
import org.molgenis.bbmri.directory.model.Filter;
import org.molgenis.bbmri.directory.model.Query;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.molgenis.bbmri.directory.controller.DirectoryController.URI;
import static org.molgenis.bbmri.directory.model.Collection.createCollection;
import static org.molgenis.bbmri.directory.model.Filter.createFilter;
import static org.molgenis.bbmri.directory.model.Query.createQuery;

@Controller
@RequestMapping(URI)
public class DirectoryController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryController.class);

	public static final String ID = "bbmridirectory";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String VIEW_DIRECTORY = "view-directory";
	public static final String BBMRI_API = "192.168.1.188/bbmri/api/directory/create_query";

	public DirectoryController()
	{
		super(URI);
	}

	@Autowired
	Gson gson;

	@RequestMapping
	public String init(Model model)
	{
		return VIEW_DIRECTORY;
	}

	@RequestMapping("/query")
	@ResponseBody
	public void postQuery() throws Exception
	{
		LOG.info("Query received, sending request");

		// TODO create structured data

		Collection collection = createCollection("eric:collectionID:AT_MUG:collection:all_samples",
				"bbmri-eric:biobankID:AT_MUG");

		Filter filter = createFilter("name:Biobank Graz");
		List<Collection> collections = singletonList(collection);

		Query query = createQuery(collections, filter);

		System.out.println("gson = " + gson.toJson(query));
		
		// post query to 192.168.1.188/bbmri/api/directory/create_query
		RestTemplate restTemplate = new RestTemplate();

		Map<String, Object> callback = restTemplate
				.postForObject(BBMRI_API, query, Map.class);

		System.out.println("callback = " + callback);
	}

}
