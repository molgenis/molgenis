package org.molgenis.ui.menumanager;

import static org.molgenis.ui.menumanager.MenuManagerController.URI;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.validation.Valid;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;
import org.molgenis.ui.MolgenisUiMenuItemType;
import org.molgenis.ui.menu.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;

/**
 * Plugin to view and modify the app UI menu
 */
@Controller
@RequestMapping(URI)
public class MenuManagerController extends MolgenisPluginController
{
	public static final String ID = "menumanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final MenuManagerService menuManagerService;
	private final MolgenisUi molgenisUi;

	@Autowired
	public MenuManagerController(MenuManagerService menuManagerService, MolgenisUi molgenisUi)
	{
		super(URI);
		if (menuManagerService == null) throw new IllegalArgumentException("menuManagerService is null");
		if (molgenisUi == null) throw new IllegalArgumentException("molgenisUi is null");
		this.menuManagerService = menuManagerService;
		this.molgenisUi = molgenisUi;
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		List<MolgenisUiMenuItem> menus = new TreeTraverser<MolgenisUiMenuItem>()
		{
			@Override
			public Iterable<MolgenisUiMenuItem> children(MolgenisUiMenuItem root)
			{
				if (root.getType() == MolgenisUiMenuItemType.MENU)
				{
					MolgenisUiMenu menu = (MolgenisUiMenu) root;
					return Iterables.filter(menu.getItems(), new Predicate<MolgenisUiMenuItem>()
					{
						@Override
						public boolean apply(MolgenisUiMenuItem molgenisUiMenuItem)
						{
							return molgenisUiMenuItem.getType() == MolgenisUiMenuItemType.MENU;
						}
					});
				}
				else return Collections.emptyList();
			}
		}.preOrderTraversal(molgenisUi.getMenu()).toList();

		List<MolgenisPlugin> plugins = Lists.newArrayList(menuManagerService.getPlugins());
		Collections.sort(plugins, new Comparator<MolgenisPlugin>()
		{
			@Override
			public int compare(MolgenisPlugin molgenisPlugin1, MolgenisPlugin molgenisPlugin2)
			{
				return molgenisPlugin1.getId().compareTo(molgenisPlugin2.getId());
			}
		});

		model.addAttribute("menus", menus);
		model.addAttribute("plugins", plugins);
		model.addAttribute("molgenis_ui", molgenisUi);
		return "view-menumanager";
	}

	@RequestMapping(value = "/save", method = POST)
	@ResponseStatus(OK)
	public void save(@Valid @RequestBody Menu molgenisMenu)
	{
		menuManagerService.saveMenu(molgenisMenu);
	}
}
