package org.molgenis.ontology.controller;

import static org.molgenis.ontology.controller.SortaServiceAnonymousController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ontology.sorta.SortaService;
import org.molgenis.ontology.utils.SortaServiceUtil;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class SortaServiceAnonymousController extends MolgenisPluginController
{
	@Autowired
	private DataService dataService;

	@Autowired
	private SortaService sortaService;

	@Autowired
	private FileStore fileStore;

	public static final String VIEW_NAME = "ontology-match-annonymous-view";
	public static final String ID = "sorta_anonymous";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public SortaServiceAnonymousController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("ontologies", SortaServiceUtil.getEntityAsMap(sortaService.getAllOntologyEntities()));
		return VIEW_NAME;
	}
}