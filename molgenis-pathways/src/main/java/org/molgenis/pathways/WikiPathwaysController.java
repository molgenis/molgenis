package org.molgenis.pathways;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.validation.Valid;

import org.molgenis.dataWikiPathways.WSPathwayInfo;
import org.molgenis.dataWikiPathways.WSSearchResult;
import org.molgenis.dataWikiPathways.WikiPathways;
import org.molgenis.dataWikiPathways.WikiPathwaysPortType;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.molgenis.pathways.WikiPathwaysController.URI;

@Controller
@RequestMapping(URI)
public class WikiPathwaysController extends MolgenisPluginController
{
	//FIXME: proper exception handling
	
	public static final String ID = "wikipathways";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final WikiPathways wikiPathways = new WikiPathways();
	public static final WikiPathwaysPortType service = wikiPathways.getWikiPathwaysSOAPPortHttp();
	public static final String organism = "Homo sapiens";
	
	@Autowired
	private FileStore fileStore;

	
	
	public WikiPathwaysController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model) throws MalformedURLException
	{
		Map<String, String> pathwayNames = getListOfPathwayNames();
		model.addAttribute("listOfPathwayNames", pathwayNames);
		
		return "view-WikiPathways";
	}

	// No spring annotations, used by methods in this class
	@RequestMapping(value="/allPathways", method = POST)
	@ResponseBody
	private Map<String, String> getListOfPathwayNames()
	{
		// model.addAttribute("firstOrganism", service.listOrganisms().get(0));
		Map<String, String> pathwayNames = new HashMap<String, String>();
		List<WSPathwayInfo> listOfPathways = service.listPathways(organism);

		int count = 0;

		for (WSPathwayInfo info : listOfPathways)
		{
			pathwayNames.put(info.getId(), info.getName());
			if (count == 5)
			{
				break;
			}
			count = count + 1;
		}

		return pathwayNames;
	}

	// With spring annotation, can be called via url by, for example javascript
	@RequestMapping(value = "/geneName", method = POST)
	@ResponseBody
	public Map<String, String> getPathwayByGeneName(@Valid @RequestBody String submittedGene)
	{
		Map<String, String> pathwayNames2 = new HashMap<String, String>();
		List<WSSearchResult> listOfPathways2 = service.findPathwaysByText(submittedGene, organism);
		
		for (WSSearchResult info2 : listOfPathways2)
		{
			pathwayNames2.put(info2.getId(), info2.getName());
		}

		return pathwayNames2;
	}
	
	@RequestMapping(value = "/pathwayViewer/{pathwayId}", method = GET)
	@ResponseBody
	public String getPathway(@PathVariable String pathwayId) throws MalformedURLException
	{
		
		byte[] source = service.getPathwayAs("svg", pathwayId, 0);
		ByteArrayInputStream bis = new ByteArrayInputStream(source);
		
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(bis);
		scanner.useDelimiter("\\Z");//To read all scanner content in one String
		String pathway = "";
		if (scanner.hasNext()) pathway = scanner.next();

		return pathway;
	}
}