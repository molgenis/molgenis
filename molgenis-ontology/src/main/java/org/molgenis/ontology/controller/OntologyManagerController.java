package org.molgenis.ontology.controller;

import static org.molgenis.ontology.controller.OntologyManagerController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collections;
import java.util.Map;

import org.molgenis.file.FileStore;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ontology.core.service.OntologyService;
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
		return Collections.singletonMap("results", ontologyService.getOntologies());
	}
}
