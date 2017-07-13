package org.molgenis.pathways;

import com.google.common.collect.Multimap;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.pathways.model.Impact;
import org.molgenis.pathways.model.Pathway;
import org.molgenis.pathways.service.WikiPathwaysService;
import org.molgenis.ui.MolgenisPluginController;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.BinaryOperator.maxBy;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.pathways.WikiPathwaysController.URI;
import static org.molgenis.util.stream.MultimapCollectors.toArrayListMultimap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class WikiPathwaysController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(WikiPathwaysController.class);

	private static final String ID = "pathways";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final Pattern EFFECT_PATTERN = Pattern.compile(
			"([A-Z]*\\|)(\\|*[0-9]+\\||\\|+)+([0-9A-Z]+)(\\|*)(.*)");

	private static final String HOMO_SAPIENS = "Homo sapiens";
	public static final String EFFECT_ATTRIBUTE_NAME = "EFF";
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
	}

	/**
	 * Shows the start screen.
	 *
	 * @param model the {@link Model} to fill
	 * @return the view name
	 */
	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("entitiesMeta", getVCFEntities());
		return "view-pathways";
	}

	/**
	 * Retrieves the list of VCF entities. They are recognized by the fact that they have an effect attribute.
	 *
	 * @return {@link List} of {@link EntityType} for the VCF entities
	 */
	private List<EntityType> getVCFEntities()
	{
		return stream(dataService.getEntityTypeIds().spliterator(), false).map(dataService::getEntityType)
																		  .filter(this::hasEffectAttribute)
																		  .collect(toList());
	}

	/**
	 * Determines if an entity has an effect attribute.
	 *
	 * @param emd {@link EntityType} of the entity
	 * @return boolean indicating if the entity has an effect column
	 */
	private boolean hasEffectAttribute(EntityType emd)
	{
		return emd.getAttribute(EFFECT_ATTRIBUTE_NAME) != null;
	}

	/**
	 * Retrieves all pathways.
	 *
	 * @return {@link Collection} of all {@link Pathway}s.
	 * @throws ExecutionException if load from cache fails
	 */
	@RequestMapping(value = "/allPathways", method = POST)
	@ResponseBody
	public Collection<Pathway> getAllPathways() throws ExecutionException
	{
		return wikiPathwaysService.getAllPathways(HOMO_SAPIENS);
	}

	/**
	 * Searches pathways.
	 *
	 * @param searchTerm string to search for
	 * @return {@link Collection} of all {@link Pathway}s found for searchTerm
	 * @throws RemoteException
	 * @throws ExecutionException
	 */
	@RequestMapping(value = "/filteredPathways", method = POST)
	@ResponseBody
	public Collection<Pathway> getFilteredPathways(@RequestBody String searchTerm)
			throws RemoteException, ExecutionException
	{
		if (StringUtils.isEmpty(searchTerm))
		{
			return getAllPathways();
		}
		return wikiPathwaysService.getFilteredPathways(searchTerm, HOMO_SAPIENS);
	}

	/**
	 * Retrieves uncolored pathway image.
	 *
	 * @param pathwayId the id of the pathway
	 * @return single-line svg string of the pathway image
	 * @throws ExecutionException if load from cache fails
	 */
	@RequestMapping(value = "/pathwayViewer/{pathwayId}", method = GET)
	@ResponseBody
	public String getPathway(@PathVariable String pathwayId) throws ExecutionException
	{
		return wikiPathwaysService.getUncoloredPathwayImage(pathwayId);
	}

	/**
	 * Retrieves gene symbols plus impact from the EFF attributes of all {@link Entity}s in a VCF repository that have
	 * an EFF attribute containing a gene symbol.
	 *
	 * @param selectedVcf name of the VCF {@link Repository}
	 * @return Map mapping Gene name to highest {@link Impact} for that gene
	 */
	private Map<String, Impact> getGenesForVcf(String selectedVcf)
	{
		return stream(dataService.getRepository(selectedVcf).spliterator(), false).map(
				entity -> entity.getString(EFFECT_ATTRIBUTE_NAME))
																				  .filter(eff -> !StringUtils.isEmpty(
																						  getGeneFromEffect(eff)))
																				  .collect(groupingBy(
																						  WikiPathwaysController::getGeneFromEffect,
																						  reducing(Impact.NONE,
																								  WikiPathwaysController::getImpactFromEffect,
																								  maxBy(Enum::compareTo))));
	}

	/**
	 * Parses the impact from an effect attribute. Recognizes the strings HIGH, MODERATE, and LOW.
	 *
	 * @param eff String value of the effect attribute
	 * @return the highest {@link Impact} found in the effect attribute, or {@link Impact#NONE} if none found
	 */
	private static Impact getImpactFromEffect(String eff)
	{
		return eff.contains("HIGH") ? Impact.HIGH : eff.contains("MODERATE") ? Impact.MODERATE : eff.contains(
				"LOW") ? Impact.LOW : Impact.NONE;
	}

	/**
	 * Parses the Gene symbol from an effect attribute.
	 *
	 * @param eff String value of the effect attribute
	 * @return the gene symbol or null if none found
	 */
	private static String getGeneFromEffect(String eff)
	{
		if (!StringUtils.isEmpty(eff))
		{
			Matcher effectMatcher = EFFECT_PATTERN.matcher(eff);
			if (effectMatcher.find())
			{
				return effectMatcher.group(3);
			}
		}
		return null;
	}

	/**
	 * Retrieves all pathways for the genes in a vcf.
	 *
	 * @param selectedVcf the name of the vcf {@link Repository}
	 * @return {@link Collection} of {@link Pathway}s found for genes in the VCF
	 * @throws ExecutionException if the loading from cache fails
	 */
	@RequestMapping(value = "/pathwaysByGenes", method = POST)
	@ResponseBody
	public Collection<Pathway> getListOfPathwayNamesByGenes(@Valid @RequestBody String selectedVcf)
			throws ExecutionException
	{
		return getGenesForVcf(selectedVcf).keySet()
										  .stream()
										  .map(this::getPathwaysForGene)
										  .flatMap(Collection::stream)
										  .collect(toCollection(LinkedHashSet::new));
	}

	/**
	 * Retrieves all pathways for a gene
	 *
	 * @param gene the HGNC name of the gene
	 * @return Collection of {@link Pathway}s
	 */
	private Collection<Pathway> getPathwaysForGene(String gene)
	{
		try
		{
			return wikiPathwaysService.getPathwaysForGene(gene, HOMO_SAPIENS);
		}
		catch (ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves a colored pathway.
	 *
	 * @param selectedVcf name of the VCF {@link Repository}
	 * @param pathwayId   ID of the pathway
	 * @return svg for the pathway, with the genes in the VCF colored according to their {@link Impact}
	 * @throws ParserConfigurationException if the creation of the {@link DocumentBuilder} fails
	 * @throws IOException                  If any IO errors occur when parsing the GPML
	 * @throws SAXException                 If any parse errors occur when parsing the GPML
	 * @throws ExecutionException           if the loading of the colored pathway from cache fails
	 */
	@RequestMapping(value = "/getColoredPathway/{selectedVcf}/{pathwayId}", method = GET)
	@ResponseBody
	public String getColoredPathway(@PathVariable String selectedVcf, @PathVariable String pathwayId)
			throws ParserConfigurationException, SAXException, IOException, ExecutionException
	{
		return getColoredPathway(selectedVcf, pathwayId, analyzeGPML(wikiPathwaysService.getPathwayGPML(pathwayId)));
	}

	/**
	 * Analyses pathway GPML. Determines for each gene in which graphIds it is displayed.
	 *
	 * @param gpml String containing the pathway GPML
	 * @return {@link Multimap} mapping gene symbol to graphIDs
	 * @throws IllegalArgumentException if the gpml is invalid
	 */
	Multimap<String, String> analyzeGPML(String gpml) throws ParserConfigurationException, IOException, SAXException
	{
		return streamDataNodes(gpml).filter(node -> !node.getAttribute("GraphId").isEmpty())
									.collect(toArrayListMultimap(node -> getGeneSymbol(node.getAttribute("TextLabel")),
											node -> node.getAttribute("GraphId")));
	}

	/**
	 * Finds the DataNode elements in a gpml string.
	 *
	 * @param gpml String containing the gpml document
	 * @return {@link Stream} of DataNode {@link Element}s
	 * @throws IllegalArgumentException if the gpml is invalid
	 */
	private Stream<Element> streamDataNodes(String gpml)
	{
		Document document;
		try
		{
			document = DB_FACTORY.newDocumentBuilder().parse(new InputSource(new StringReader(gpml)));
			NodeList dataNodes = document.getElementsByTagName("DataNode");
			return range(0, dataNodes.getLength()).mapToObj(dataNodes::item).map(Element.class::cast);
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			LOG.error("Invalid GPML " + gpml);
			throw new IllegalArgumentException("Invalid GPML");
		}
	}

	/**
	 * Finds the gene symbol in a text label.
	 *
	 * @param textLabel the text label to look in
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
	 * @param selectedVcf     the name of the vcf entity
	 * @param pathwayId       the id of the pathway in WikiPathways
	 * @param graphIdsPerGene {@link Multimap} mapping gene symbol to graphId
	 * @return String svg from for the pathway
	 * @throws ExecutionException if the loading from cache fails
	 */
	private String getColoredPathway(String selectedVcf, String pathwayId, Multimap<String, String> graphIdsPerGene)
			throws ExecutionException
	{
		Map<String, Impact> impactPerGraphId = new HashMap<>();
		getGenesForVcf(selectedVcf).forEach(
				(gene, impact) -> graphIdsPerGene.get(gene).forEach(graphId -> impactPerGraphId.put(graphId, impact)));

		if (!impactPerGraphId.isEmpty())
		{
			return wikiPathwaysService.getColoredPathwayImage(pathwayId, impactPerGraphId);
		}
		else
		{
			return wikiPathwaysService.getUncoloredPathwayImage(pathwayId);
		}
	}
}