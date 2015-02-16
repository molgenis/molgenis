package org.molgenis.pathways;

import static org.molgenis.pathways.WikiPathwaysController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
import org.molgenis.dataWikiPathways.WikiPathwaysPortType;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Controller
@RequestMapping(URI)
public class WikiPathwaysController extends MolgenisPluginController
{
//	private static final int GENES_PER_PATHWAY_LOOKUP_BATCH_SIZE = 20;
	private static final String COLORS2 = "colors";
	private static final String GRAPH_IDS = "graphIds";
	private static final String PATHWAY_ID = "pathwayId";

	private static final Logger LOG = LoggerFactory.getLogger(WikiPathwaysController.class);
	// FIXME: proper exception handling

	private static final String ID = "wikipathways";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private final WikiPathwaysPortType wikiPathwaysService;
	private static final String HOMO_SAPIENS = "Homo sapiens";
	private Map<String, List<String>> nodeList = new HashMap<>();
	private static Map<Integer, String> variantColor = new HashMap<Integer, String>();
	private Map<String, String> pathwayNames;

	private final LoadingCache<String, List<WSPathwayInfo>> allPathwaysCache = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<WSPathwayInfo>>()
			{
				@Override
				public List<WSPathwayInfo> load(String organism) throws Exception
				{
					List<WSPathwayInfo> listPathways = wikiPathwaysService.listPathways(organism);
					return listPathways;

				}
			});

	private final LoadingCache<String, String> uncoloredPathwayImageCache = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, String>()
			{
				@Override
				public synchronized String load(String pathwayId) throws Exception
				{
					return toSingleLineString(wikiPathwaysService.getPathwayAs("svg", pathwayId, 0));

				}
			});

	private static String toSingleLineString(byte[] source)
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(source);
		Scanner scanner = new Scanner(bis);
		scanner.useDelimiter("\\Z");// To read all scanner content in one String
		String pathway = "";
		try
		{
			if (scanner.hasNext())
			{
				pathway = scanner.next();
			}
		}
		finally
		{
			scanner.close();
		}
		return pathway;
	}

	private final LoadingCache<String, List<WSSearchResult>> pathwaysByXrefCache = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<WSSearchResult>>()
			{
				public List<WSSearchResult> load(String gene) throws Exception
				{
					List<WSSearchResult> listPathways = wikiPathwaysService.findPathwaysByXref(Collections.singletonList(gene),
							Collections.singletonList("H")); // H for HGNC database (human gene symbols)
					return listPathways;
				}
			});

	private final LoadingCache<Map<String, Object>, byte[]> COLORED_PATHWAY_IMAGE_CACHED = CacheBuilder.newBuilder()
			.maximumSize(Integer.MAX_VALUE).refreshAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<Map<String, Object>, byte[]>()
			{
				@SuppressWarnings(
				{ "rawtypes", "unchecked" })
				public byte[] load(Map<String, Object> coloredPathwayParameters) throws Exception
				{
					List a = (List) coloredPathwayParameters.get(GRAPH_IDS);
					List b = (List) coloredPathwayParameters.get(COLORS2);
					return wikiPathwaysService.getColoredPathway(coloredPathwayParameters.get(PATHWAY_ID).toString(),
							"0", a, b, "svg");
				}
			});

	@Autowired
	private DataService dataService;

	@Autowired
	public WikiPathwaysController(WikiPathwaysPortType wikiPathwaysService)
	{
		super(URI);
		this.wikiPathwaysService = wikiPathwaysService;
		fillVariantColors();
	}

	private void fillVariantColors()
	{
		variantColor.put(3, "FF0000"); // red
		variantColor.put(2, "FFA500"); // orange
		variantColor.put(1, "FFFF00"); // yellow
		variantColor.put(0, "219AD7"); // lighter blue, so the gene symbol is still visible
	}

	/**
	 * Shows the start screen.
	 * 
	 * @param model
	 *            the {@link Model} to fill
	 * @return the view name
	 */
	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("listOfPathwayNames", pathwayNames);
		model.addAttribute("entitiesMeta", getVCFEntities());
		model.addAttribute("selectedEntityName", "");
		return "view-WikiPathways";
	}

	/**
	 * Retrieves the list of VCF entities. They are recognized by the fact that they have an "EFF" attribute.
	 * 
	 * @return {@link List} of {@link EntityMetaData} for the VCF entities
	 */
	private List<EntityMetaData> getVCFEntities()
	{
		List<EntityMetaData> entitiesMeta = new ArrayList<EntityMetaData>();
		for (String entityName : dataService.getEntityNames())
		{
			EntityMetaData emd = dataService.getEntityMetaData(entityName);
			if (emd.getAttribute("EFF") != null)
			{
				entitiesMeta.add(emd);
			}
		}
		return entitiesMeta;
	}

	/**
	 * Retrieves all pathways.
	 * 
	 * @return Map with all pathway ids mapped to pathway name
	 * @throws ExecutionException
	 *             if load from cache fails
	 */
	@RequestMapping(value = "/allPathways", method = POST)
	@ResponseBody
	public Map<String, String> getAllPathways() throws ExecutionException
	{
		List<WSPathwayInfo> listOfPathways = allPathwaysCache.get(HOMO_SAPIENS);

		Map<String, String> result = new HashMap<String, String>();
		for (WSPathwayInfo pathwayInfo : listOfPathways)
		{
			result.put(pathwayInfo.getId(), pathwayInfo.getName());
		}
		return result;
	}

	/**
	 * Searches pathways.
	 * 
	 * @param searchTerm
	 *            string to search for
	 * @return Map with all matching pathway ids mapped to pathway name
	 */
	@RequestMapping(value = "/filteredPathways", method = POST)
	@ResponseBody
	public Map<String, String> getFilteredPathways(@Valid @RequestBody String searchTerm)
	{
		List<WSSearchResult> pathwaysByText = wikiPathwaysService.findPathwaysByText(searchTerm, HOMO_SAPIENS);

		Map<String, String> result = new HashMap<String, String>();
		for (WSSearchResult pathwayInfo : pathwaysByText)
		{
			result.put(pathwayInfo.getId(), pathwayInfo.getName());
		}
		return result;
	}

	/**
	 * Retrieves pathway image.
	 * 
	 * @param pathwayId
	 *            the id of the pathway
	 * @return single-line svg string of the pathway image
	 * @throws ExecutionException
	 *             if load from cache failsÏ
	 */
	@RequestMapping(value = "/pathwayViewer/{pathwayId}", method = GET)
	@ResponseBody
	public String getPathway(@PathVariable String pathwayId) throws ExecutionException
	{
		return uncoloredPathwayImageCache.get(pathwayId);
	}

	/**
	 * Retrieves gene symbols plus impact for a VCF.
	 * 
	 * @param selectedVcf
	 *            name of the VCF to select
	 * @return Map mapping Gene name to highest impact
	 */
	private HashMap<String, Integer> getGenesForVcf(String selectedVcf)
	{
		LOG.info("getGenesForVcf() selectedVcf = " + selectedVcf);
		// TODO: cache result per VCF!
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		Repository repository = dataService.getRepositoryByEntityName(selectedVcf);
		// TODO: Nog even kijken naar de regex
		Pattern effectPattern = Pattern.compile("([A-Z]*\\|)(\\|*[0-9]+\\||\\|+)+([0-9A-Z]+)(\\|*)(.*)");
		Iterator<Entity> iterator = repository.iterator();
		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			String eff = entity.getString("EFF");
			if (!StringUtils.isEmpty(eff))
			{
				Matcher effectMatcher = effectPattern.matcher(eff);
				if (effectMatcher.find())
				{
					String geneSymbol = effectMatcher.group(3);
					int impact = eff.contains("HIGH") ? 3 : eff.contains("MODERATE") ? 2 : eff.contains("LOW") ? 1 : 0;

					if (result.containsKey(geneSymbol))
					{
						if (impact > result.get(geneSymbol))
						{
							result.put(geneSymbol, impact);
						}
					}
					else
					{
						result.put(geneSymbol, impact);
					}
				}
			}
		}
		return result;
	}

	@RequestMapping(value = "/pathwaysByGenes", method = POST)
	@ResponseBody
	public Map<String, String> getListOfPathwayNamesByGenes(@Valid @RequestBody String selectedVcf)
			throws ExecutionException
	{
		LOG.info("getListOfPathwayNamesByGenes() selectedVcf = " + selectedVcf);
		Map<String, Integer> highestImpactPerGene = getGenesForVcf(selectedVcf);
		Map<String, String> result = new HashMap<String, String>();
		for(String gene: highestImpactPerGene.keySet())
		{
			result.putAll(geneToPathways(gene));
		}
		return result;
	}

	private Map<String, String> geneToPathways(String gene) throws ExecutionException 
	{
		LOG.info("geneToPathways()"+gene);
		Map<String, String> result = new HashMap<String, String>();
		List<WSSearchResult> listOfPathwaysByGenes = new ArrayList<WSSearchResult>();
		listOfPathwaysByGenes = pathwaysByXrefCache.get(gene);
		for (WSSearchResult info3 : listOfPathwaysByGenes)
		{
			if (HOMO_SAPIENS.equals(info3.getSpecies()))
			{
				result.put(info3.getId(), info3.getName() + " (" + info3.getId() + ")");
			}
		}
		return result;
	}

	@RequestMapping(value = "/getGPML/{selectedVcf}/{pathwayId}", method = GET)
	@ResponseBody
	public String getGPML(@PathVariable String selectedVcf, @PathVariable String pathwayId)
			throws ParserConfigurationException, SAXException, IOException, ExecutionException
	{
		nodeList = new HashMap<String, List<String>>();
		WSPathway wsPathway = wikiPathwaysService.getPathway(pathwayId, 0);
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

		return getColoredPathway(selectedVcf, pathwayId, nodeList);
	}

	String getGeneSymbol(String textLabel)
	{
		String geneSymbols = "";
		Pattern geneSymbolPattern = Pattern.compile("^[0-9A-Za-z\\-]*");
		if (textLabel.contains("&quot;"))
		{
			System.out.println("WARNING: textlabel(" + textLabel + ") contains quotes, removing those...");
			textLabel = textLabel.replace("&quot;", "");// FIXME: nasty construction, but wikipathways data is not
														// consistent. How to do this properly
		}
		Matcher geneSymbolMatcher = geneSymbolPattern.matcher(textLabel);
		if (geneSymbolMatcher.find())
		{
			geneSymbols = geneSymbolMatcher.group(0);
		}
		return geneSymbols;
	}

	private String getColoredPathway(String selectedVcf, String pathwayId, Map<String, List<String>> nodeList)
			throws ExecutionException
	{
		Map<Integer, List<String>> graphIdToColor = new HashMap<Integer, List<String>>();
		List<String> colors = new ArrayList<String>();
		List<String> graphIds = new ArrayList<String>();
		byte[] base64Binary = null;

		// genes: geneSymbol, impact
		// variantColor: impact, color
		// nodeList: geneSymbol, graphId

		Map<String, Integer> highestImpactPerGene = getGenesForVcf(selectedVcf);

		for (String gene : highestImpactPerGene.keySet())
		{
			int impact = highestImpactPerGene.get(gene);

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

		for (int j : graphIdToColor.keySet())
		{
			// For each graphId for this impact, we put the impact color in the color list
			for (int i = 0; i < graphIdToColor.get(j).size(); i++)
			{
				colors.add(variantColor.get(j));
			}
			graphIds.addAll(graphIdToColor.get(j));
		}

		// Check if graphIds and colors are empty
		if (!graphIds.isEmpty() && !colors.isEmpty())
		{
			Map<String, Object> coloredPathwayParameters = new HashMap<String, Object>();

			coloredPathwayParameters.put(PATHWAY_ID, pathwayId);
			coloredPathwayParameters.put(GRAPH_IDS, graphIds);
			coloredPathwayParameters.put(COLORS2, colors);

			// System.out.println(coloredPathwayParameters);
			base64Binary = COLORED_PATHWAY_IMAGE_CACHED.get(coloredPathwayParameters);
			// base64Binary = idsToPathways(graphIds, colors, pathwayId);
			ByteArrayInputStream bis = new ByteArrayInputStream(base64Binary);

			Scanner scan = new Scanner(bis);
			scan.useDelimiter("\\Z");// To read all scanner content in one String
			String coloredPathway = "";
			if (scan.hasNext()) coloredPathway = scan.next();
			scan.close();

			return coloredPathway.replace("<svg", "<svg viewBox='0 0 1000 1500'");
		}
		else
		{
			System.out.println("normal pathway, uncolored");
			return uncoloredPathwayImageCache.get(pathwayId);
		}
	}
}