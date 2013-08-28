package org.molgenis.ui;

import static org.molgenis.ui.MolgenisMenuController.URI;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_CONTEXT_URL;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_MOLGENIS_UI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

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

	@RequestMapping("/{menuId}/{pluginId}/**")
	public String forwardMenuPlugin(HttpServletRequest request, @Valid @NotNull @PathVariable String menuId,
			@Valid @NotNull @PathVariable String pluginId, Model model)
	{
		String contextUri = new StringBuilder(URI).append('/').append(menuId).append('/').append(pluginId).toString();
		String mappingUri = (String) (request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		String remainder = mappingUri.substring(contextUri.length());

		model.addAttribute(KEY_CONTEXT_URL, contextUri);
		model.addAttribute(KEY_MENU_ID, menuId);
		return getForwardPluginUri(pluginId, remainder);
	}

	private String getForwardPluginUri(String pluginId, String pathRemainder)
	{
		StringBuilder strBuilder = new StringBuilder("forward:");
		strBuilder.append(MolgenisPlugin.PLUGIN_URI_PREFIX);
		strBuilder.append(pluginId);
		if (pathRemainder != null) strBuilder.append(pathRemainder);
		return strBuilder.toString();
	}
}
