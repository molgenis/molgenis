package org.molgenis.pathways;

import static org.molgenis.pathways.WikiPathwaysController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bridgedb.bio.Organism;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.pathvisio.core.model.ConverterException;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@Controller
@RequestMapping(URI)
public class WikiPathwaysController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(WikiPathwaysController.class);

	private static final String ID = "wikipathways";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final Pattern EFFECT_PATTERN = Pattern
			.compile("([A-Z]*\\|)(\\|*[0-9]+\\||\\|+)+([0-9A-Z]+)(\\|*)(.*)");

	private static final Organism HOMO_SAPIENS = Organism.HomoSapiens;
	private static Map<Integer, String> variantColor = new HashMap<Integer, String>();
	private Map<String, String> pathwayNames;
	private final WikiPathwaysService wikiPathwaysService;
	@Autowired
	private DataService dataService;
	private static final Pattern GENE_SYMBOL_PATTERN = Pattern.compile("^[0-9A-Za-z\\-]*");
	private static final DocumentBuilderFactory DB_FACTORY = DocumentBuilderFactory.newInstance();

	@Autowired
	public WikiPathwaysController(WikiPathwaysService wikiPathwaysService)
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
		return wikiPathwaysService.getAllPathways(HOMO_SAPIENS);
	}
	
	/**
	 * Searches pathways.
	 * 
	 * @param searchTerm
	 *            string to search for
	 * @return Map with all matching pathway ids mapped to pathway name
	 * @throws RemoteException 
	 */
	@RequestMapping(value = "/filteredPathways", method = POST)
	@ResponseBody
	public Map<String, String> getFilteredPathways(@Valid @RequestBody String searchTerm) throws RemoteException
	{
		return wikiPathwaysService.getFilteredPathways(searchTerm, HOMO_SAPIENS);
	}

	/**
	 * Retrieves pathway image.
	 * 
	 * @param pathwayId
	 *            the id of the pathway
	 * @return single-line svg string of the pathway image
	 * @throws ExecutionException
	 *             if load from cache fails
	 */
	@RequestMapping(value = "/pathwayViewer/{pathwayId}", method = GET)
	@ResponseBody
	public String getPathway(@PathVariable String pathwayId) throws ExecutionException
	{
		return wikiPathwaysService.getUncoloredPathwayImage(pathwayId);
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
		Iterator<Entity> iterator = repository.iterator();
		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			updateEffect(result, entity.getString("EFF"));
		}
		return result;
	}

	/**
	 * Interprets effect and updates maximum impact.
	 * 
	 * @param maximumImpacts
	 *            Map with maximum impact per gene
	 * @param eff
	 *            String with effect attribute of VCF
	 */

	private void updateEffect(HashMap<String, Integer> maximumImpacts, String eff)
	{
		if (!StringUtils.isEmpty(eff))
		{
			Matcher effectMatcher = EFFECT_PATTERN.matcher(eff);
			if (effectMatcher.find())
			{
				String geneSymbol = effectMatcher.group(3);
				int impact = eff.contains("HIGH") ? 3 : eff.contains("MODERATE") ? 2 : eff.contains("LOW") ? 1 : 0;

				if (!maximumImpacts.containsKey(geneSymbol) || impact > maximumImpacts.get(geneSymbol))
				{
					maximumImpacts.put(geneSymbol, impact);
				}
			}
		}
	}

	/**
	 * Retrieves all pathways for the genes in a vcf.
	 * 
	 * @param selectedVcf
	 *            the name of the vcf entity
	 * @return Map mapping pathway ID to pathway name plus ID
	 * @throws ExecutionException
	 *             if the loading from cache fails
	 */
	@RequestMapping(value = "/pathwaysByGenes", method = POST)
	@ResponseBody
	public Map<String, String> getListOfPathwayNamesByGenes(@Valid @RequestBody String selectedVcf)
			throws ExecutionException
	{
		LOG.info("getListOfPathwayNamesByGenes() selectedVcf = " + selectedVcf);
		Map<String, String> result = new HashMap<String, String>();
		for (String gene : getGenesForVcf(selectedVcf).keySet())
		{
			result.putAll(getPathwaysForGene(gene));
		}
		return result;
	}

	/**
	 * Retrieves all pathways for a gene
	 * 
	 * @param gene
	 *            the HGNC name of the gene
	 * @return Map mapping pathway ID to pathway name plus ID
	 * @throws ExecutionException
	 *             if the loading from cache fails
	 */
	private Map<String, String> getPathwaysForGene(String gene) throws ExecutionException
	{
		LOG.debug("getPathwaysForGene()" + gene);
		return wikiPathwaysService.getPathwaysForGene(gene, HOMO_SAPIENS);
	}

	@RequestMapping(value = "/getGPML/{selectedVcf}/{pathwayId}", method = GET)
	@ResponseBody
	public String getGPML(@PathVariable String selectedVcf, @PathVariable String pathwayId)
			throws ParserConfigurationException, SAXException, IOException, ExecutionException, ConverterException
	{
		Multimap<String, String> graphIdsPerGene = analyzeGPML(wikiPathwaysService.getCurrentPathwayGPML(pathwayId));
		return getColoredPathway(selectedVcf, pathwayId, graphIdsPerGene);
	}

	/**
	 * Analyses pathway GPML. Determines for each gene in which graphIds it is displayed.
	 * 
	 * @param gpml
	 *            String containing the pathway GPML
	 * @return {@link Multimap} mapping gene symbol to graphIDs
	 * @throws ParserConfigurationException
	 *             if the creation of the {@link DocumentBuilder} fails
	 * @throws IOException
	 *             If any IO errors occur when parsing the GPML
	 * @throws SAXException
	 *             If any parse errors occur when parsing the GPML
	 */
	private Multimap<String, String> analyzeGPML(String gpml) throws ParserConfigurationException, IOException,
			SAXException
	{
		DocumentBuilder dBuilder = DB_FACTORY.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(gpml.getBytes());
		Document doc = dBuilder.parse(is);
		NodeList dataNodes = doc.getElementsByTagName("DataNode");

		Multimap<String, String> result = ArrayListMultimap.create();
		for (int i = 0; i < dataNodes.getLength(); i++)
		{
			Element dataNode = (Element) dataNodes.item(i);
			String graphId = dataNode.getAttribute("GraphId");
			String textLabel = dataNode.getAttribute("TextLabel");

			String geneSymbol = getGeneSymbol(textLabel);
			if (!graphId.isEmpty())
			{
				result.put(geneSymbol, graphId);
			}
		}
		return result;
	}

	/**
	 * Finds the gene symbol in a text label.
	 * 
	 * @param textLabel
	 *            the text label to look in
	 * @return Gene symbol if found, otherwise ""
	 */
	String getGeneSymbol(String textLabel)
	{
		String geneSymbol = "";
		if (textLabel.contains("&quot;"))
		{
			LOG.warn("Textlabel(" + textLabel
					+ ") contains quotes, which is inconsistent with the gene names. Removing the quotes.");
			textLabel = textLabel.replace("&quot;", "");
		}
		Matcher geneSymbolMatcher = GENE_SYMBOL_PATTERN.matcher(textLabel);
		if (geneSymbolMatcher.find())
		{
			geneSymbol = geneSymbolMatcher.group(0);
		}
		return geneSymbol;
	}

	/**
	 * Retrieves a colored pathway. Sometimes WikiPathways returns no graphIds for a pathway, then the pathway is
	 * returned uncolored.
	 * 
	 * @param selectedVcf
	 *            the name of the vcf entity
	 * @param pathwayId
	 *            the id of the pathway in WikiPathways
	 * @param graphIdsPerGene
	 *            {@link Multimap} mapping gene symbol to graphId
	 * @return String svg from for the pathway
	 * @throws ExecutionException
	 *             if the loading from cache fails
	 */
	private String getColoredPathway(String selectedVcf, String pathwayId, Multimap<String, String> graphIdsPerGene)
			throws ExecutionException
	{
		Map<String, Integer> highestImpactPerGene = getGenesForVcf(selectedVcf);
		Map<String, Integer> highestImpactPerGraphId = new HashMap<String, Integer>();

		for (String gene : highestImpactPerGene.keySet())
		{
			int impact = highestImpactPerGene.get(gene);
			for (String graphId : graphIdsPerGene.get(gene))
			{
				highestImpactPerGraphId.put(graphId, impact);
			}
		}

		List<String> colors = new ArrayList<String>();
		List<String> graphIds = new ArrayList<String>();
		for (String graphId : highestImpactPerGraphId.keySet())
		{
			Integer impact = highestImpactPerGraphId.get(graphId);
			String color = variantColor.get(impact);
			graphIds.add(graphId);
			colors.add(color);
		}

		if (!graphIds.isEmpty())
		{
			return wikiPathwaysService.getColoredPathwayImage(pathwayId, graphIds, colors);
		}
		else
		{
			LOG.warn("normal pathway, uncolored");
			return wikiPathwaysService.getUncoloredPathwayImage(pathwayId);
		}
	}
}