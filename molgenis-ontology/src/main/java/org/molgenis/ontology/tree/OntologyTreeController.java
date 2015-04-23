package org.molgenis.ontology.tree;

import static org.molgenis.ontology.tree.OntologyTreeController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ontology.matching.OntologyService;
import org.molgenis.ontology.utils.OntologyServiceUtil;
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
		model.addAttribute("ontologies", OntologyServiceUtil.getEntityAsMap(ontologyService.getAllOntologyEntities()));
		return "ontology-tree-view";
	}
}