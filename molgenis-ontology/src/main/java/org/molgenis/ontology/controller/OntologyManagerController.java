package org.molgenis.ontology.controller;

import static org.molgenis.ontology.controller.OntologyManagerController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.file.FileStore;
import org.molgenis.ontology.matching.OntologyService;
import org.molgenis.ontology.utils.OntologyServiceUtil;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class OntologyManagerController extends MolgenisPluginController
{
	public static final String ID = "ontologymanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String ONTOLOGY_MANAGER_PLUGIN = "OntologyManagerPlugin";

	@Autowired
	private FileStore fileStore;

	@Autowired
	private OntologyService ontologyService;

	public OntologyManagerController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		return ONTOLOGY_MANAGER_PLUGIN;
	}

	@RequestMapping(value = "/ontology", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> getAllOntologies()
	{
		Map<String, Object> results = new HashMap<String, Object>();
		List<Map<String, Object>> ontologies = new ArrayList<Map<String, Object>>();
		for (Entity entity : ontologyService.getAllOntologyEntities())
		{
			ontologies.add(OntologyServiceUtil.getEntityAsMap(entity));
		}
		results.put("results", ontologies);
		return results;
	}
}
