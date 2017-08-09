package org.molgenis.ui.security;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;
import org.molgenis.ui.MolgenisUiMenuItemType;

import java.util.List;

public class MolgenisUiMenuPermissionDecorator implements MolgenisUiMenu
{
	private final MolgenisUiMenu molgenisUiMenu;
	private final MolgenisPermissionService molgenisPermissionService;

	public MolgenisUiMenuPermissionDecorator(MolgenisUiMenu molgenisUiMenu,
			MolgenisPermissionService molgenisPermissionService)
	{
		if (molgenisUiMenu == null) throw new IllegalArgumentException("menu is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenisPermissionService is null");
		this.molgenisUiMenu = molgenisUiMenu;
		this.molgenisPermissionService = molgenisPermissionService;
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
	public MolgenisUiMenuItemType getType()
	{
		return molgenisUiMenu.getType();
	}

	@Override
	public MolgenisUiMenu getParentMenu()
	{
		return molgenisUiMenu.getParentMenu();
	}

	@Override
	public boolean isAuthorized()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<MolgenisUiMenuItem> getItems()
	{
		return Lists.newArrayList(Iterables.filter(
				Iterables.transform(molgenisUiMenu.getItems(), molgenisUiMenuItem ->
				{
					switch (molgenisUiMenuItem.getType())
					{
						case MENU:
							return new MolgenisUiMenuPermissionDecorator((MolgenisUiMenu) molgenisUiMenuItem,
									molgenisPermissionService);
						case PLUGIN:
							return molgenisUiMenuItem;
						default:
							throw new RuntimeException(
									"Unknown MolgenisUiMenuItem [" + molgenisUiMenuItem.getType() + "]");
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
	public MolgenisUiMenuItem getActiveItem()
	{
		return Iterables.find(molgenisUiMenu.getItems(), molgenisUiMenuItem -> molgenisUiMenuItem.getType() != MolgenisUiMenuItemType.MENU && hasPermission(molgenisUiMenuItem), null);
	}

	@Override
	public List<MolgenisUiMenu> getBreadcrumb()
	{
		return molgenisUiMenu.getBreadcrumb();
	}

	private boolean hasPermission(MolgenisUiMenuItem molgenisUiMenuItem)
	{
		boolean hasPermission;
		switch (molgenisUiMenuItem.getType())
		{
			case MENU:
				hasPermission = !((MolgenisUiMenu) molgenisUiMenuItem).getItems().isEmpty();
				break;
			case PLUGIN:
				String menuItemId = molgenisUiMenuItem.getId();
				hasPermission = molgenisPermissionService.hasPermissionOnPlugin(menuItemId, Permission.READ);
				break;
			default:
				throw new RuntimeException("Unknown MolgenisUiMenuItem [" + molgenisUiMenuItem.getType() + "]");
		}
		return hasPermission;
	}
}
