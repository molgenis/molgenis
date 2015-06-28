package org.molgenis.hpofilter;

import static org.molgenis.hpofilter.HpoFilterController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.hpofilter.data.GeneMapProvider;
import org.molgenis.hpofilter.data.HGNCLocations;
import org.molgenis.hpofilter.data.HpoFilterDataProvider;
import org.molgenis.hpofilter.data.Locus;
import org.molgenis.hpofilter.utils.HgncLocationsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class HpoFilterController extends MolgenisPluginController
{
	public static final String ID = "hpofilter";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-hpofilter";
	private final DataService dataService;
	private final HpoFilterDataProvider hpoFilterDataProvider;
	private GeneMapProvider hgncProvider;
	
	private HashMap<String, String> autoCompletionMap;

	@Autowired
	public HpoFilterController(DataService dataService,
			HpoFilterDataProvider hpoFilterDataProvider,
			GeneMapProvider hgncProvider,
			RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		super(URI);
		this.dataService = dataService;
		this.hpoFilterDataProvider = hpoFilterDataProvider;
		this.hgncProvider = hgncProvider;
	}

	@RequestMapping
	public String showView(@RequestParam(value = "entity", required = false) String selectedEntityName, Model model)
	{
		boolean showEntitySelect = true;
		List<EntityMetaData> emds = Lists.newArrayList();
		
		for (String entityName : dataService.getEntityNames())
		{
			if (currentUserHasRole(AUTHORITY_SU, AUTHORITY_ENTITY_READ_PREFIX + entityName.toUpperCase()))
			{
				if (null != dataService.getRepository(entityName).getEntityMetaData().getAttribute("#CHROM") &&
						null != dataService.getRepository(entityName).getEntityMetaData().getAttribute("POS")) {
					emds.add(dataService.getEntityMetaData(entityName));
					if (StringUtils.isNotBlank(selectedEntityName) && selectedEntityName.equalsIgnoreCase(entityName))
					{
						// Hide entity dropdown
						showEntitySelect = false;
					}
				}
			}
		}

		model.addAttribute("showEntitySelect", showEntitySelect);
		if (showEntitySelect)
		{
			if (StringUtils.isNotBlank(selectedEntityName))
			{
				// selectedEntityName not found -> show warning
				model.addAttribute("warningMessage",
						"Entity does not exist or you do not have permission on this entity");
			}

			if (!emds.isEmpty())
			{
				// Select first entity
				selectedEntityName = emds.get(0).getName();
			}
		}

		model.addAttribute("entitiesMeta", emds);
		model.addAttribute("selectedEntityName", selectedEntityName);
		return VIEW_NAME;
	}
	
	private String getTermSuggestionMarkup(String hpo, String phenotype) {
		String html = "<button type=\"button\" class=\"list-group-item term-select\" id=\""+hpo+"\">"
				+ hpo
				+ " - "
				+ phenotype
				+ "</button>";
		return html;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ac")
	private @ResponseBody String autoComplete (@RequestParam(value = "search", required = true) String search)
	{
		int count = 0;
		ArrayList<String> results = new ArrayList<>();
		StringBuilder response = new StringBuilder();
		if (null == autoCompletionMap)
			this.autoCompletionMap = hpoFilterDataProvider.getDescriptionMap();
		for (String desc : autoCompletionMap.keySet()) {
			if (desc.contains(search)) {
				results.add(getTermSuggestionMarkup(autoCompletionMap.get(desc),desc));
				count++;
			}
		}
		Collections.sort(results);
		for (String item : results) {
			response.append(item+"\n");
		}
		return response.toString();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/filter")
	private @ResponseBody String filter(@RequestParam(value = "terms", required = true) String[] inputList,
			@RequestParam(value = "entity", required = true) String selectedEntityName,
			@RequestParam(value = "target", required = false) String targetEntityName,
			Model model) {
		try{
			Repository repository;
			Repository newRepository;
			List<Entity> filteredEntities;
			String chrom;
			Long pos;
			Locus locus;
			List<String> genes;
			EntityMetaData entityMetaData;
			String newEntityName;
			DefaultEntityMetaData newEntityMetaData;
			HashMap<Integer, Stack<HpoFilterInput>> inputSet = new HashMap<Integer, Stack<HpoFilterInput>>();
			Long count = 0L;
			Long pass = 0L;
			Long fail = 0L;
			Long size;
			
			if (null != selectedEntityName) {
				repository = dataService.getRepository(selectedEntityName);
				entityMetaData = repository.getEntityMetaData();
				size = repository.count();
				if (null == repository.getEntityMetaData().getAttribute("#CHROM"))
					throw new RuntimeException("Entity does not contain required attribute '#CHROM'");
				if (null == repository.getEntityMetaData().getAttribute("POS"))
					throw new RuntimeException("Entity does not contain required attribute 'POS'");
			}else{
				throw new RuntimeException("No entity has been selected");
			}

			if (null == targetEntityName || targetEntityName.isEmpty()) 
				newEntityName = selectedEntityName+"_filtered_hpofilter";
			else
				newEntityName = targetEntityName;
			
			for (String input : inputList) {
				String[] hpoProperties = input.split("-");
				int group = Integer.parseInt(hpoProperties[0]);
				int id = Integer.parseInt(hpoProperties[1]);
				boolean recursive = Boolean.parseBoolean(hpoProperties[2]);
				String hpo = hpoProperties[3];
				if (!inputSet.containsKey(group))
					inputSet.put(group, new Stack<HpoFilterInput>());
				inputSet.get(group).add(new HpoFilterInput(group, id, recursive, hpo));
				System.out.println(hpo+" is in group "+group+", has ID "+id+" and has recursive set to "+recursive);
			}
			
			
			filteredEntities = new Stack<Entity>();
			
			Map<String, HGNCLocations> hgncLocations = hgncProvider.getHgncLocations();
			
			Iterator<Entity> e = repository.iterator();
			while (e.hasNext()) {
				Entity entity = e.next();
				chrom = entity.getString("#CHROM");
				pos = entity.getLong("POS");
				locus =  new Locus(chrom, pos);
				genes = HgncLocationsUtils.locationToHgcn(hgncLocations, locus);
				
				for (String gene : genes) {
					if (inputContainsGene(inputSet, gene)) {
						filteredEntities.add(entity);
						pass++;
						break;
					}
				}
				count++;
				if (count%500 == 0)
					fail = count - pass;
					System.out.println("Filtered "+count+"/"+size+" entities ("+(100*count)/size+"%). PASS/FAIL: "+pass+"/"+fail);
			}
			repository.close();
			
			if (!filteredEntities.isEmpty()) {
				System.out.println("Adding "+pass+" filtered variants to "+newEntityName);
				newEntityMetaData = new DefaultEntityMetaData(newEntityName,
						entityMetaData);
				newEntityMetaData.setLabel(newEntityName);
				newRepository = dataService.getMeta().addEntityMeta(newEntityMetaData);
				
				newRepository.add(filteredEntities);
				
				newRepository.close();
				return "Filtering succeded";
			}else {
				return "Filtering succeded, no entities passed filter.";
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("An unknown error occured when filtering");
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/exists")
	private @ResponseBody boolean repoExists(@RequestParam(value = "entityName", required=true) String entityName) {
		return dataService.hasRepository(entityName);
	}

	private boolean inputContainsGene(HashMap<Integer, Stack<HpoFilterInput>> inputGroups, String gene) {
		for (Stack<HpoFilterInput> inputGroup : inputGroups.values()) {
			if (inputGroupContainsGene(inputGroup, gene)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean inputGroupContainsGene(Stack<HpoFilterInput> inputGroup, String gene) {
		for (HpoFilterInput input : inputGroup) {
			if (!hpoContainsGene(input.hpo(), gene, input.recursive())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if a specified hpo contains a specified gene. If 
	 * recursive = true, it will also check the specified HPO's 
	 * children.
	 * @param hpo the filter HPO term
	 * @param gene the variants' gene
	 * @param recursive true if searching children, false if not.
	 * @return true if HPO contains gene, false if hpo does not contain gene
	 */
	private boolean hpoContainsGene(String hpo, String gene, boolean recursive) {
		if (hpoFilterDataProvider.getAssocData().containsKey(hpo)) 
			if (hpoFilterDataProvider.getAssocData().get(hpo).contains(gene))
				return true;
			if (recursive)
				for (String child : hpoFilterDataProvider.getHPOData().get(hpo))
					if (null != child && hpoContainsGene(child, gene, true))
						return true;
		return false;
	}
}