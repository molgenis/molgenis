package org.molgenis.catalogue;

import static org.molgenis.catalogue.CatalogueController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class CatalogueController extends MolgenisPluginController
{
	public static final String ID = "catalogue";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-catalogue";
	private final DataService dataService;

	@Autowired
	public CatalogueController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping
	public String showView(final @RequestParam(value = "entity", required = false) String selectedEntityName,
			Model model)
	{
		AtomicBoolean showEntitySelectBoolean = new AtomicBoolean(true);
		List<EntityMetaData> emds = Lists.newArrayList();
		dataService.getEntityNames().forEach(entityName -> {
			if (currentUserHasRole(AUTHORITY_SU, AUTHORITY_ENTITY_READ_PREFIX + entityName.toUpperCase()))
			{
				emds.add(dataService.getEntityMetaData(entityName));
				if (StringUtils.isNotBlank(selectedEntityName) && selectedEntityName.equalsIgnoreCase(entityName))
				{
					// Hide entity dropdown
					showEntitySelectBoolean.set(false);
				}
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
				selectedEntityNameValue = emds.get(0).getName();
			}
		}

		model.addAttribute("entitiesMeta", emds);
		model.addAttribute("selectedEntityName", selectedEntityNameValue);

		return VIEW_NAME;
	}
}