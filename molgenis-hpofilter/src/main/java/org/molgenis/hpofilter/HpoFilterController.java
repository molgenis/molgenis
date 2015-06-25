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
	
	@RequestMapping(method = RequestMethod.GET, value = "/ac")
	public @ResponseBody String autoComplete (@RequestParam(value = "search", required = true) String search)
	{
		ArrayList<String> results = new ArrayList<>();
		StringBuilder response = new StringBuilder();
		if (null == autoCompletionMap)
			this.autoCompletionMap = hpoFilterDataProvider.getDescriptionMap();
		for (String desc : autoCompletionMap.keySet()) {
			if (desc.matches(search))
				results.add("<li>"+autoCompletionMap.get(desc)+" - "+desc+"</li>");
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
			String chrom;
			Long pos;
			Locus locus;
			List<String> genes;
			EntityMetaData entityMetaData;
			String newEntityName;
			DefaultEntityMetaData newEntityMetaData;
			HashMap<Integer, Stack<HpoFilterInput>> inputSet = new HashMap<Integer, Stack<HpoFilterInput>>();
			
			
			if (null != selectedEntityName) {
				repository = dataService.getRepository(selectedEntityName);
				entityMetaData = repository.getEntityMetaData();
				if (null == repository.getEntityMetaData().getAttribute("#CHROM"))
					throw new RuntimeException("Entity does not contain required attribute '#CHROM'");
				if (null == repository.getEntityMetaData().getAttribute("POS"))
					throw new RuntimeException("Entity does not contain required attribute 'POS'");
			}else{
				throw new RuntimeException("No entity has been selected");
			}

			if (null == targetEntityName || targetEntityName.isEmpty()) 
				newEntityName = selectedEntityName+"-filtered-hpofilter";
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
			
			newEntityMetaData = new DefaultEntityMetaData(newEntityName,
					entityMetaData);
			
			newEntityMetaData.setLabel(newEntityName);
			
			newRepository = dataService.getMeta().addEntityMeta(newEntityMetaData);
			
			Map<String, HGNCLocations> hgncLocations = hgncProvider.getHgncLocations();
			
			Iterator<Entity> e = repository.iterator();
			while (e.hasNext()) {
				Entity entity = e.next();
				chrom = entity.getString("#CHROM");
				pos = entity.getLong("POS");
				locus =  new Locus(chrom, pos);
				genes = HgncLocationsUtils.locationToHgcn(hgncLocations, locus);
				System.out.println("Checking variant at "+pos+" to be added to repo "+newRepository.getName());
				
				for (String gene : genes) {
					if (inputContainsGene(inputSet, gene)) {
						System.out.println("Adding variant at "+pos+" to repo "+newRepository.getName());
						newRepository.add(entity);
						break;
					}
				}
			}
			repository.close();
			return "Filtering succeded";
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