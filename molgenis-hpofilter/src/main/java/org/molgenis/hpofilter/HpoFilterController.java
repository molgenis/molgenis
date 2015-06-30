package org.molgenis.hpofilter;

import static org.molgenis.hpofilter.HpoFilterController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.tree.DefaultAttribute;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
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
	private Map<String, HGNCLocations> hgncLocations;
	private HpoFilterLogic hpoFilterLogic;
	
	private HashMap<String, String> autoCompletionMap;

	@Autowired
	public HpoFilterController(DataService dataService,
			HpoFilterDataProvider hpoFilterDataProvider,
			GeneMapProvider hgncProvider,
			RepositoryDecoratorFactory repositoryDecoratorFactory,
			HpoFilterLogic hpoFilterLogic)
	{
		super(URI);
		this.dataService = dataService;
		this.hpoFilterDataProvider = hpoFilterDataProvider;
		this.hgncProvider = hgncProvider;
		this.hpoFilterLogic = hpoFilterLogic;
	}

	@RequestMapping
	public String showView(@RequestParam(value = "entity", required = false) String selectedEntityName, Model model)
	{
		boolean showEntitySelect = true;
		List<EntityMetaData> emds = Lists.newArrayList();
		
		try {
			hgncLocations = hgncProvider.getHgncLocations();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		String html = "<button type=\"button\" class=\"list-group-item term-select btn-block text-left\" id=\""+hpo+"\">"
				+ "<span class=\"glyphicon glyphicon-chevron-left\"></span>"
				+ hpo
				+ " - "
				+ phenotype
				+ "</button>";
		return html;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ac")
	private @ResponseBody String autoComplete (@RequestParam(value = "search", required = true) String search)
	{
		ArrayList<String> results = new ArrayList<>();
		StringBuilder response = new StringBuilder();
		if (null == autoCompletionMap)
			this.autoCompletionMap = hpoFilterDataProvider.getDescriptionMap();
		for (String desc : autoCompletionMap.keySet()) {
				if (desc.toLowerCase().contains(search.toLowerCase())) {
					results.add(getTermSuggestionMarkup(autoCompletionMap.get(desc),desc));
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
				if (null == repository.getEntityMetaData().getAttribute("#CHROM")) {
					return "danger%Entity does not contain required attribute '#CHROM'";
				}
				if (null == repository.getEntityMetaData().getAttribute("POS")) {
					return "danger%Entity does not contain required attribute 'POS'";
				}
			}else{
				return "danger%No entity has been selected";
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
				if (!inputSet.containsKey(group)) {
					inputSet.put(group, new Stack<HpoFilterInput>());
				}
				inputSet.get(group).add(new HpoFilterInput(group, id, recursive, hpo));
				System.out.println(hpo+" is in group "+group+", has ID "+id+" and has recursive set to "+recursive);
			}
			
			newEntityMetaData = new DefaultEntityMetaData(newEntityName,
					entityMetaData);
			newEntityMetaData.setLabel(newEntityName);
			
			AttributeMetaData symbolsMetaData = new DefaultAttributeMetaData("GENE_SYMBOLS", MolgenisFieldTypes.FieldTypeEnum.STRING);
			AttributeMetaData inputMetaData = new DefaultAttributeMetaData("FILTER_INPUT", MolgenisFieldTypes.FieldTypeEnum.STRING);
			DefaultAttributeMetaData filterAnnotationMetaData = new DefaultAttributeMetaData("HPO_filter", MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
			filterAnnotationMetaData.addAttributePart(symbolsMetaData);
			filterAnnotationMetaData.addAttributePart(inputMetaData);
			newEntityMetaData.addAttributeMetaData(filterAnnotationMetaData);
			
			filteredEntities = new Stack<Entity>();
			
			Iterator<Entity> e = repository.iterator();
			while (e.hasNext()) {
				Entity entity = new MapEntity(e.next(), newEntityMetaData);
				chrom = entity.getString("#CHROM");
				pos = entity.getLong("POS");
				locus =  new Locus(chrom, pos);
				genes = HgncLocationsUtils.locationToHgcn(hgncLocations, locus);
				
				for (String gene : genes) {
					if (hpoFilterLogic.inputContainsGene(inputSet, gene)) {
						entity.set("GENE_SYMBOLS", StringUtils.join(genes,","));
						entity.set("FILTER_INPUT", hpoFilterLogic.getInputString(inputSet));
						filteredEntities.add(entity);
						pass++;
						break;
					}
				}
				
				count++;
				if (count%500 == 0) {
					fail = count - pass;
					System.out.println("Filtered "+count+"/"+size+" entities ("+(100*count)/size+"%). PASS/FAIL: "+pass+"/"+fail);
				}
			}
			repository.close();
			
			if (!filteredEntities.isEmpty()) {
				System.out.println("Adding "+pass+" filtered variants to "+newEntityName);
				
				newRepository = dataService.getMeta().addEntityMeta(newEntityMetaData);
				
				newRepository.add(filteredEntities);
				
				newRepository.close();
				return "success%Filtering succeded. Added "+pass+" filtered variants to "+newEntityName+". <a href=\"http://localhost:8080/menu/main/dataexplorer?entity="+newEntityName+"\">Click to view results</a>";
			}else {
				return "success%Filtering succeded. No entities passed filter, and no new entity has been created.";
			}
		}catch (Exception e) {
			e.printStackTrace();
			return "danger%An unknown error occured when filtering. Please try again.";
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/exists")
	private @ResponseBody boolean repoExists(@RequestParam(value = "entityName", required=true) String entityName)
	{
		return dataService.hasRepository(entityName);
	}
}