package org.molgenis.wikiPathwaysController;

import java.util.List;

import org.molgenis.dataWikiPathways.WSPathwayInfo;
import org.molgenis.dataWikiPathways.WikiPathways;
import org.molgenis.dataWikiPathways.WikiPathwaysPortType;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.molgenis.wikiPathwaysController.WikiPathwaysController.URI;

@Controller
@RequestMapping(URI)
public class WikiPathwaysController extends MolgenisPluginController
{

	public static final String ID = "wikipathways";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final WikiPathways wikiPathways = new WikiPathways();
	public static final WikiPathwaysPortType service = wikiPathways.getWikiPathwaysSOAPPortHttp();
	public static final String organism = "Homo sapiens";
	
	public WikiPathwaysController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String getListOfOrganisms(Model model)
	{
		// model.addAttribute("firstOrganism", service.listOrganisms().get(0));
		List<WSPathwayInfo> listOfPathways = service.listPathways(organism);

//		for (int i = 0; i < listOfPathways.size(); i++)
//		{
//			WSPathwayInfo info = listOfPathways.get(i);
//			model.addAttribute("firstPathway",info.getName());
//		}
		WSPathwayInfo info = listOfPathways.get(0);
		model.addAttribute("firstPathway",info.getName());
		
		return "view-WikiPathways";
	}
}
