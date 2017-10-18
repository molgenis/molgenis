package org.molgenis.catalogue;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.molgenis.catalogue.CatalogueController.URI;

@Controller
@RequestMapping(URI)
public class CatalogueController extends PluginController
{
	public static final String ID = "catalogue";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-catalogue";
	private final DataService dataService;

	public CatalogueController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@GetMapping
	public String showView(final @RequestParam(value = "entity", required = false) String selectedEntityName,
			Model model)
	{
		AtomicBoolean showEntitySelectBoolean = new AtomicBoolean(true);
		List<EntityType> emds = Lists.newArrayList();
		dataService.getEntityTypeIds().forEach(entityTypeId ->
		{
			emds.add(dataService.getEntityType(entityTypeId));
			if (StringUtils.isNotBlank(selectedEntityName) && selectedEntityName.equalsIgnoreCase(entityTypeId))
			{
				// Hide entity dropdown
				showEntitySelectBoolean.set(false);
			}
		});
		boolean showEntitySelect = showEntitySelectBoolean.get();

		model.addAttribute("showEntitySelect", showEntitySelect);

		String selectedEntityNameValue = selectedEntityName;
		if (showEntitySelect)
		{
			if (StringUtils.isNotBlank(selectedEntityNameValue))
			{
				// selectedEntityName not found -> show warning
				model.addAttribute("warningMessage",
						"Entity does not exist or you do not have permission on this entity");
			}

			if (!emds.isEmpty())
			{
				// Select first entity
				selectedEntityNameValue = emds.get(0).getId();
			}
		}

		model.addAttribute("entitiesMeta", emds);
		model.addAttribute("selectedEntityName", selectedEntityNameValue);

		return VIEW_NAME;
	}
}