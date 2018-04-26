package org.molgenis.core.ui.security;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.molgenis.web.UiMenuItemType;

import java.util.List;

public class MolgenisUiMenuPermissionDecorator implements UiMenu
{
	private final UiMenu molgenisUiMenu;
	private final UserPermissionEvaluator permissionService;

	public MolgenisUiMenuPermissionDecorator(UiMenu molgenisUiMenu, UserPermissionEvaluator permissionService)
	{
		if (molgenisUiMenu == null) throw new IllegalArgumentException("menu is null");
		if (permissionService == null) throw new IllegalArgumentException("permissionService is null");
		this.molgenisUiMenu = molgenisUiMenu;
		this.permissionService = permissionService;
	}

	@Override
	public String getId()
	{
		return molgenisUiMenu.getId();
	}

	@Override
	public String getName()
	{
		return molgenisUiMenu.getName();
	}

	@Override
	public String getUrl()
	{
		return molgenisUiMenu.getUrl();
	}

	@Override
	public UiMenuItemType getType()
	{
		return molgenisUiMenu.getType();
	}

	@Override
	public UiMenu getParentMenu()
	{
		return molgenisUiMenu.getParentMenu();
	}

	@Override
	public boolean isAuthorized()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<UiMenuItem> getItems()
	{
		return Lists.newArrayList(Iterables.filter(Iterables.transform(molgenisUiMenu.getItems(), molgenisUiMenuItem ->
		{
			switch (molgenisUiMenuItem.getType())
			{
				case MENU:
					return new MolgenisUiMenuPermissionDecorator((UiMenu) molgenisUiMenuItem, permissionService);
				case PLUGIN:
					return molgenisUiMenuItem;
				default:
					throw new UnexpectedEnumException(molgenisUiMenuItem.getType());
			}
		}), this::hasPermission));
	}

	@Override
	public boolean containsItem(final String itemId)
	{
		return Iterables.any(molgenisUiMenu.getItems(),
				molgenisUiMenuItem -> molgenisUiMenuItem.getId().equals(itemId) && hasPermission(molgenisUiMenuItem));
	}

	@Override
	public UiMenuItem getActiveItem()
	{
		return Iterables.find(molgenisUiMenu.getItems(),
				molgenisUiMenuItem -> molgenisUiMenuItem.getType() != UiMenuItemType.MENU && hasPermission(
						molgenisUiMenuItem), null);
	}

	@Override
	public List<UiMenu> getBreadcrumb()
	{
		return molgenisUiMenu.getBreadcrumb();
	}

	private boolean hasPermission(UiMenuItem molgenisUiMenuItem)
	{
		boolean hasPermission;
		switch (molgenisUiMenuItem.getType())
		{
			case MENU:
				hasPermission = !((UiMenu) molgenisUiMenuItem).getItems().isEmpty();
				break;
			case PLUGIN:
				String menuItemId = molgenisUiMenuItem.getId();
				hasPermission = permissionService.hasPermission(new PluginIdentity(menuItemId), PluginPermission.READ);
				break;
			default:
				throw new UnexpectedEnumException(molgenisUiMenuItem.getType());
		}
		return hasPermission;
	}
}
