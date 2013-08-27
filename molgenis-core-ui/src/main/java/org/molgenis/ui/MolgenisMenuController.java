package org.molgenis.ui;

import static org.molgenis.ui.MolgenisHeaderAttributes.KEY_MOLGENIS_UI;
import static org.molgenis.ui.MolgenisMenuController.URI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class MolgenisMenuController
{
	public static final String URI = "/menu";

	static final String KEY_MENU_ID = "menu_id";

	private final MolgenisUi molgenisUi;

	@Autowired
	public MolgenisMenuController(MolgenisUi molgenisUi)
	{
		if (molgenisUi == null) throw new IllegalArgumentException("molgenisUi is null");
		this.molgenisUi = molgenisUi;
	}

	@RequestMapping
	public String forwardDefaultMenuDefaultPlugin(Model model)
	{
		MolgenisUiMenu menu = molgenisUi.getMenu();
		if (menu == null) throw new RuntimeException("main menu does not exist");
		model.addAttribute(KEY_MENU_ID, menu.getId());

		MolgenisUiMenuItem activeItem = menu.getActiveItem();
		if (activeItem == null) throw new RuntimeException("Warning! Main menu does not contain any (accessible) items");

		return getForwardPluginUri(activeItem.getId(), null);
	}

	@RequestMapping("/{menuId}")
	public String forwardMenuDefaultPlugin(@Valid @NotNull @PathVariable String menuId, Model model)
	{
		MolgenisUiMenu menu = molgenisUi.getMenu(menuId);
		if (menu == null) throw new RuntimeException("menu with id [" + menuId + "] does not exist");
		model.addAttribute(KEY_MENU_ID, menuId);

		MolgenisUiMenuItem activeItem = menu.getActiveItem();
		if (activeItem == null)
		{
			model.addAttribute(KEY_MOLGENIS_UI, molgenisUi);
			return "view-empty";
		}

		return getForwardPluginUri(activeItem.getId(), null);
	}

	@RequestMapping("/{menuId}/{pluginId}")
	public String forwardMenuPlugin(@Valid @NotNull @PathVariable String menuId,
			@Valid @NotNull @PathVariable String pluginId, Model model)
	{
		model.addAttribute(KEY_MENU_ID, menuId);
		return getForwardPluginUri(pluginId, null);
	}

	@RequestMapping("/{menuId}/{pluginId}/{remainder:.+}")
	public String forwardMenuPlugin(@Valid @NotNull @PathVariable String menuId,
			@Valid @NotNull @PathVariable String pluginId, @PathVariable String remainder, Model model)
	{
		model.addAttribute(KEY_MENU_ID, menuId);
		return getForwardPluginUri(pluginId, remainder);
	}

	private String getForwardPluginUri(String pluginId, String pathRemainder)
	{
		StringBuilder strBuilder = new StringBuilder("forward:");
		strBuilder.append(MolgenisPluginController.PLUGIN_URI_PREFIX);
		strBuilder.append(pluginId);
		if (pathRemainder != null) strBuilder.append('/').append(pathRemainder);
		return strBuilder.toString();
	}
}
