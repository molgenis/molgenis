package org.molgenis.pathways;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

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
	public final WikiPathwaysPortType service;
	public final String organism = "Homo sapiens";
	public Map<String, Integer> genes = new HashMap<>();
	/**
	 * Maps text label onto graphId
	 */
	// public final Map<String, String> nodeList = new HashMap<>();
	public Map<Integer, String> variantColor = new HashMap<>();

	private Map<String, String> pathwayNames;

	@Autowired
	DataService dataService;

	@Autowired
	public WikiPathwaysController(WikiPathwaysPortType service)
	{
		super(URI);
		this.service = service;
	}

	@RequestMapping(method = GET)
	public String init(Model model) throws IOException
	{
		if (pathwayNames == null)
		{
			// pathwayNames = getListOfPathwayNames();
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
		variantColor = new HashMap<>();
		genes = new HashMap<>();
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
				if (impact > genes.get(geneSymbol))
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
//		variantColor.put(0, "0000FF"); // blue
		variantColor.put(0, "219AD7"); //lighter blue, so the gene symbol is still visible
	}

	@RequestMapping(value = "/pathwaysByGenes", method = POST)
	@ResponseBody
	public Map<String, String> getListOfPathwayNamesByGenes()
	{
		Map<String, String> pathwayByGenes = new HashMap<String, String>();
		List<String> geneSymbols = new ArrayList<String>();

		for (String symbol : genes.keySet())
		{
			geneSymbols.add(symbol);
		}

		List<String> temporaryList = new ArrayList<String>();
		// String query = "";
		List<String> genesForPathwaySearch = new ArrayList<String>();

		for (int i = 0; i < geneSymbols.size(); i++)
		{
			String e = geneSymbols.get(i);
			temporaryList.add(e);

			if (i % 20 == 0 && i != 0)
			{
				System.out.println("processing genes");
				// genesToPathways(pathwayByGenes, temporaryList, query);
				genesToPathways(pathwayByGenes, temporaryList, genesForPathwaySearch);
				temporaryList.clear();
			}
		}
		if (temporaryList.size() != 0)
		{
			// genesToPathways(pathwayByGenes, temporaryList, query);
			genesToPathways(pathwayByGenes, temporaryList, genesForPathwaySearch);
		}
		return pathwayByGenes;
	}

	// private void genesToPathways(Map<String, String> pathwayByGenes, List<String> temporaryList, String query)
	private void genesToPathways(Map<String, String> pathwayByGenes, List<String> temporaryList,
			List<String> genesForPathwaySearch)
	{
		List<WSSearchResult> listOfPathwaysByGenes = new ArrayList<WSSearchResult>();
		for (String gene : temporaryList)
		{
			// query += gene + " ";
			genesForPathwaySearch.add(gene);
		}
		// listOfPathwaysByGenes = service.findPathwaysByText(query, organism);
		List<String> codes = new ArrayList<String>();
		codes.add("H"); // H for HGNC database (human gene symbols)
		listOfPathwaysByGenes = service.findPathwaysByXref(genesForPathwaySearch, codes);

		for (WSSearchResult info3 : listOfPathwaysByGenes)
		{
			if (info3.getSpecies().equals("Homo sapiens"))
			{
				pathwayByGenes.put(info3.getId(), info3.getName()+" ("+info3.getId()+")");
				
			}

		}
	}

	@RequestMapping(value = "/getGPML/{pathwayId}", method = GET)
	@ResponseBody
	public String getGPML(@PathVariable String pathwayId) throws ParserConfigurationException, SAXException,
			IOException
	{
		Map<String, List<String>> nodeList = new HashMap<>();

		WSPathway wsPathway = service.getPathway(pathwayId, 0);
		String gpml = wsPathway.getGpml();
//		System.out.println(gpml);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(gpml.getBytes());
		Document doc = dBuilder.parse(is);
		NodeList dataNodes = doc.getElementsByTagName("DataNode");

		for (int i = 0; i < dataNodes.getLength(); i++)
		{
			Element dataNode = (Element) dataNodes.item(i);
			String graphId = dataNode.getAttribute("GraphId");
			// graphIdList.add(graphId);
			String textLabel = dataNode.getAttribute("TextLabel");
			// System.out.println(textLabel);

			String geneSymbol = getGeneSymbol(textLabel);
			if (graphId.isEmpty())
			{
				continue;
			}
			if (nodeList.containsKey(geneSymbol))
			{ // if a gene is already in the map
				nodeList.get(geneSymbol).add(graphId); // get the list, and add the graphId
			}
			else
			{
				List<String> graphIdList = new ArrayList<String>();
				graphIdList.add(graphId);
				nodeList.put(geneSymbol, graphIdList);
			}

		}
		System.out.println(nodeList);

		return getColoredPathway(pathwayId, nodeList);
	}

	String getGeneSymbol(String textLabel)
	{
		String geneSymbols = "";
		Pattern pat = Pattern.compile("^[0-9A-Za-z\\-]*");
		if (textLabel.contains("&quot;"))
		{
			System.out.println("WARNING: textlabel(" + textLabel + ") contains quotes, removing those...");
			textLabel = textLabel.replace("&quot;", "");// FIXME: nasty construction, but wikipathways data is not
														// consistent. How to do this properly
		}
		Matcher mat = pat.matcher(textLabel);
		if (mat.find())
		{
			geneSymbols = mat.group(0);
		}
		else
		{
			return null;
		}
		return geneSymbols;
	}

	private String getColoredPathway(String pathwayId2, Map<String, List<String>> nodeList)
	{
		Map<Integer, List<String>> graphIdToColor = new HashMap<Integer, List<String>>();
		List<String> colors = new ArrayList<String>();
		List<String> graphIds = new ArrayList<String>();
		byte[] base64Binary = null;

		// genes: geneSymbol, impact
		// variantColor: impact, color
		// nodeList: geneSymbol, graphId

		for (String gene : genes.keySet())
		{
			int impact = genes.get(gene);
//			System.out.println("NODELIST KEYS: " + nodeList.keySet());
//			System.out.println("GENES KEYS: " + genes.keySet());
			if (nodeList.containsKey(gene))
			{
				if (graphIdToColor.containsKey(impact))
				{ // Impact already in graphIdToColor
					graphIdToColor.get(impact).addAll(nodeList.get(gene)); // get list of graphIds for this impact and
																			// add other graphIds
				}
				else
				{
					graphIdToColor.put(impact, nodeList.get(gene)); // put new impact and first graphIds in map
				}
			}
		}

		for (int impact : graphIdToColor.keySet())
		{
			// For each graphId for this impact, we put the impact color in the color list
			for (int i = 0; i < graphIdToColor.get(impact).size(); i++)
			{
				colors.add(variantColor.get(impact));
				// graphIds.add(graphIdToColor.get(impact).get(i));
			}

			graphIds.addAll(graphIdToColor.get(impact));

		}

		System.out.println(graphIds + " " + colors);
		
		//base64Binary = service.getPathway();
		
		//Check if graphIds and colors are empty
		if(!graphIds.isEmpty() && !colors.isEmpty()){
			base64Binary = service.getColoredPathway(pathwayId2, "0", graphIds, colors, "svg");
			// base64Binary = idsToPathways(graphIds, colors, pathwayId2);
			ByteArrayInputStream bis = new ByteArrayInputStream(base64Binary);

			Scanner scan = new Scanner(bis);
			scan.useDelimiter("\\Z");// To read all scanner content in one String
			String coloredPathway = "";
			if (scan.hasNext()) coloredPathway = scan.next();
			scan.close();

			return coloredPathway;
		}
		else{
			System.out.println("normal pathway, uncolored");
			
			//if graphIds and colors are empty, getPathway() -> so uncolored
			byte[] uncoloredPathway = service.getPathwayAs("svg", pathwayId2, 0);
			ByteArrayInputStream byteis = new ByteArrayInputStream(uncoloredPathway);

			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(byteis);
			scanner.useDelimiter("\\Z");// To read all scanner content in one String
			String regulatPathway = "";
			if (scanner.hasNext()) regulatPathway = scanner.next();

			return regulatPathway;
		}
	}

	// private byte[] idsToPathways(List<String> graphIds, List<String> colors, String pathwayId2)
	// {
	// System.out.println(graphIds + " " + colors);
	//
	// byte[] base64Binary = service.getColoredPathway(pathwayId2, "0", graphIds, colors, "svg");
	// byte[] base64Binary = service.getColoredPathway(pathwayId2, "0", Arrays.asList(new String[]{"cf3", "cd6"}),
	// Arrays.asList(new String[]{"FFA500", "FF0000"}), "svg");

	// return base64Binary;
	// }

	// "FF0000" red
	// "FFA500" orange
	// "FFFF00" yellow
	// "0000FF" blue

}