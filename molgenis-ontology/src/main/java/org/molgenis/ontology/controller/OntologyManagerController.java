package org.molgenis.ontology.controller;

import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.web.PluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;

import static org.molgenis.ontology.controller.OntologyManagerController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(URI)
public class OntologyManagerController extends PluginController
{
	public static final String ID = "ontologymanager";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
	public static final String VIEW_ONTOLOGY_MANAGER = "ontology-manager-view";

	@Autowired
	private OntologyService ontologyService;

	public OntologyManagerController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		model.addAttribute("ontologies", ontologyService.getOntologies());
		return VIEW_ONTOLOGY_MANAGER;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String delete(Model model) throws Exception
	{
		model.addAttribute("ontologies", ontologyService.getOntologies());
		return VIEW_ONTOLOGY_MANAGER;
	}

	@RequestMapping(value = "/ontology", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> getAllOntologies()
	{
		return Collections.singletonMap("results", ontologyService.getOntologies());
	}
}
