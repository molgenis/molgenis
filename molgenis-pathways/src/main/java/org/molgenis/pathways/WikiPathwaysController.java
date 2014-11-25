package org.molgenis.pathways;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.dataWikiPathways.WSPathway;
import org.molgenis.dataWikiPathways.WSPathwayInfo;
import org.molgenis.dataWikiPathways.WSSearchResult;
import org.molgenis.dataWikiPathways.WikiPathways;
import org.molgenis.dataWikiPathways.WikiPathwaysPortType;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.molgenis.pathways.WikiPathwaysController.URI;

@Controller
@RequestMapping(URI)
public class WikiPathwaysController extends MolgenisPluginController
{
	// FIXME: proper exception handling

	public static final String ID = "wikipathways";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final WikiPathways wikiPathways = new WikiPathways();
	public static final WikiPathwaysPortType service = wikiPathways.getWikiPathwaysSOAPPortHttp();
	public static final String organism = "Homo sapiens";
	public static final Map<String, Integer> genes = new HashMap<>();
	public static final Map<String, String> nodeList = new HashMap<>();
	public static final Map<Integer, String> variantColor = new HashMap<>();

	private Map<String, String> pathwayNames;

	@Autowired
	DataService dataService;

	public WikiPathwaysController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model) throws IOException
	{
		if (pathwayNames == null)
		{
			pathwayNames = getListOfPathwayNames();
		}

		model.addAttribute("listOfPathwayNames", pathwayNames);

		Iterable<EntityMetaData> entitiesMeta = Iterables.transform(dataService.getEntityNames(),
				new Function<String, EntityMetaData>()
				{
					@Override
					public EntityMetaData apply(String entityName)
					{
						return dataService.getEntityMetaData(entityName);
					}
				});
		model.addAttribute("entitiesMeta", entitiesMeta);
		model.addAttribute("selectedEntityName", "");

		return "view-WikiPathways";
	}

	// No spring annotations, used by methods in this class
	@RequestMapping(value = "/allPathways", method = POST)
	@ResponseBody
	private Map<String, String> getListOfPathwayNames()
	{
		// model.addAttribute("firstOrganism", service.listOrganisms().get(0));
		Map<String, String> pathwayNames = new HashMap<String, String>();
		List<WSPathwayInfo> listOfPathways = service.listPathways(organism);

		for (WSPathwayInfo info : listOfPathways)
		{
			pathwayNames.put(info.getId(), info.getName());
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
		scanner.useDelimiter("\\Z");// To read all scanner content in one String
		String pathway = "";
		if (scanner.hasNext()) pathway = scanner.next();

		return pathway;
	}

	@RequestMapping(value = "/vcfFile", method = POST)
	@ResponseBody
	public void readVcfFile(@Valid @RequestBody String selectedVcf)
	{
		Repository repository = dataService.getRepositoryByEntityName(selectedVcf);
		Pattern p = Pattern.compile("([0-9]+|\\|+)(\\|)([A-Z]+|[A-Z]+[0-9]+)(\\|)");
		String geneSymbol = "";

		Iterator<Entity> iterator = repository.iterator();
		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			String eff = entity.getString("EFF");
			Matcher m = p.matcher(eff);
			if (m.find())
			{
				geneSymbol = m.group(3);
			}
			else
			{
				continue;
			}

			int impact = eff.contains("HIGH") ? 3 : eff.contains("MODERATE") ? 2 : eff.contains("LOW") ? 1 : 0;

			if (genes.containsKey(geneSymbol))
			{
				if (genes.get(geneSymbol) > impact)
				{
					genes.put(geneSymbol, impact);
				}
			}
			else
			{
				genes.put(geneSymbol, impact);
			}
		}
		variantColor.put(3, "FF0000"); // red
		variantColor.put(2, "FFA500"); // orange
		variantColor.put(1, "FFFF00"); // yellow
		variantColor.put(0, "0000FF"); // blue
	}

	@RequestMapping(value = "/pathwaysByGenes", method = POST)
	@ResponseBody
	private Map<String, String> getListOfPathwayNamesByGenes()
	{
		Map<String, String> pathwayByGenes = new HashMap<String, String>();
		List<String> geneSymbols = new ArrayList<String>();

		for (String symbol : genes.keySet())
		{
			geneSymbols.add(symbol);
		}

		List<WSSearchResult> listOfPathwaysByGenes = new ArrayList<WSSearchResult>();
		List<String> temporaryList = new ArrayList<String>();
		String query = "";

		for (int i = 0; i < geneSymbols.size(); i++)
		{
			String e = geneSymbols.get(i);
			temporaryList.add(e);

			if (i % 20 == 0 && i != 0)
			{
				System.out.println("processing genes");
				for (String gene : temporaryList)
				{
					query = gene + " ";
				}
				listOfPathwaysByGenes = service.findPathwaysByText(query, organism);
				for (WSSearchResult info3 : listOfPathwaysByGenes)
				{
					pathwayByGenes.put(info3.getId(), info3.getName());
				}
				temporaryList.clear();
			}
		}
		for (String gene : temporaryList)
		{
			query = gene + " ";
		}
		listOfPathwaysByGenes = service.findPathwaysByText(query, organism);

		for (WSSearchResult info3 : listOfPathwaysByGenes)
		{
			pathwayByGenes.put(info3.getId(), info3.getName());
		}

		return pathwayByGenes;
	}

	@RequestMapping(value = "/getGPML/{pathwayId}", method = GET)
	@ResponseBody
	public void getGPML(@PathVariable String pathwayId) throws ParserConfigurationException, SAXException, IOException
	{

		WSPathway wsPathway = service.getPathway(pathwayId, 0);
		String gpml = wsPathway.getGpml();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(gpml.getBytes());
		Document doc = dBuilder.parse(is);

		NodeList dataNodes = doc.getElementsByTagName("DataNode");

		for (int i = 0; i < dataNodes.getLength(); i++)
		{
			Element dataNode = (Element) dataNodes.item(i);
			String graphId = dataNode.getAttribute("GraphId");
			String textLabel = dataNode.getAttribute("TextLabel");

			nodeList.put(textLabel, graphId);
		}
		// System.out.println(nodeList);
		getColoredPathway("WP27");
	}
	
	@RequestMapping(value = "/getColoredPathway/{pathwayId}", method = GET)
	@ResponseBody
	public String getColoredPathway(@PathVariable String pathwayId)
	{		
		Map<String, Integer>genesToColor = new HashMap<String, Integer>();
//		pwId krijg je terug van gebruiker (geselecteerd in dropdown) bijvoorbeeld: WP27
//		revision = 0, altijd, voor laatste versie
//		graphId, uit nodeList (gekoppeld aan gen)
//		voor elk gene in nodelist.key en voor elk gen in genes.key, als ze gelijk zijn aan elkaar, doe gen dan in nieuwe lijst en pak de impact erbij 
//		genes.value(); Als die gelijk is aan variantcolor.key (0,1,2,3) pak dan de value(kleur). Dit wordt color attribuut	
//		String fileType  = "svg";
		
		//genes = gene, impact
		//nodeList = gene, graphId
		//variantcolor = impact, color
		
		for(String gene : genes.keySet()){
			if(nodeList.keySet().contains(gene)){
				genesToColor.put(gene, genes.get(gene));				
			}
		}
		System.out.println(genesToColor.get(0));
		
	
		return null;
	}
	
}