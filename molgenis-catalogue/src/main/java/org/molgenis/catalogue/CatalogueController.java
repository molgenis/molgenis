package org.molgenis.catalogue;

import static org.molgenis.catalogue.CatalogueController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	public String showView(@RequestParam(value = "entity", required = false) String selectedEntityName, Model model)
	{
		// TODO check permission and existence of selectedEntityName
		List<EntityMetaData> emds = Lists.newArrayList();
		for (String entityName : dataService.getEntityNames())
		{
			if (currentUserHasRole(AUTHORITY_SU, AUTHORITY_ENTITY_READ_PREFIX + entityName))
			{
				emds.add(dataService.getEntityMetaData(entityName));
			}
		}

		model.addAttribute("showEntitySelect", StringUtils.isBlank(selectedEntityName));

		if ((selectedEntityName == null) && !emds.isEmpty())
		{
			selectedEntityName = emds.get(0).getName();
		}

		model.addAttribute("entitiesMeta", emds);
		model.addAttribute("selectedEntityName", selectedEntityName);

		return VIEW_NAME;
	}

	@RequestMapping(value = "/shoppingcart", method = RequestMethod.POST)
	public void refreshShoppingCart(List<String> attributes)
	{

	}
}
