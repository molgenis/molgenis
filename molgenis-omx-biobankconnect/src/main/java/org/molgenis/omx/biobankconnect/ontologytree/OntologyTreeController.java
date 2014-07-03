package org.molgenis.omx.biobankconnect.ontologytree;

import static org.molgenis.omx.biobankconnect.ontologytree.OntologyTreeController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
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
		model.addAttribute("ontologies", ontologyService.getAllOntologies());
		return "ontology-tree-view";
	}
}