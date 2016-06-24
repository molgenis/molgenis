package org.molgenis.ontology.tree;

import static org.molgenis.ontology.tree.OntologyTreeController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class OntologyTreeController extends MolgenisPluginController
{
	@Autowired
	private OntologyService ontologyService;

	public static final String ID = "ontologytree";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public OntologyTreeController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("ontologies", ontologyService.getOntologies());
		return "ontology-tree-view";
	}
}