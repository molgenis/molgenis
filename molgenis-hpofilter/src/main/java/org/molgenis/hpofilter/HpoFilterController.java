package org.molgenis.hpofilter;

import static org.molgenis.hpofilter.HpoFilterController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.hpofilter.HpoFilterDataProvider;
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
	
	private HashMap<String, String> autoCompletionMap;

	@Autowired
	public HpoFilterController(DataService dataService, HpoFilterDataProvider hpoFilterDataProvider)
	{
		super(URI);
		this.dataService = dataService;
		this.hpoFilterDataProvider = hpoFilterDataProvider;
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
				emds.add(dataService.getEntityMetaData(entityName));
				if (StringUtils.isNotBlank(selectedEntityName) && selectedEntityName.equalsIgnoreCase(entityName))
				{
					// Hide entity dropdown
					showEntitySelect = false;
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
	
	private void run () {
		
	}
}