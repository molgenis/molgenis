package org.molgenis.ui.security;

import org.molgenis.security.core.PermissionService;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.menu.MenuUtils;

import static java.util.Objects.requireNonNull;

public class MolgenisUiPermissionDecorator implements MolgenisUi
{
	private final MolgenisUi molgenisUi;
	private final PermissionService permissionService;

	public MolgenisUiPermissionDecorator(MolgenisUi molgenisUi, PermissionService permissionService)
	{
		this.molgenisUi = requireNonNull(molgenisUi);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public MolgenisUiMenu getMenu()
	{
		MolgenisUiMenu menu = molgenisUi.getMenu();
		return menu != null ? new MolgenisUiMenuPermissionDecorator(menu, permissionService) : null;
	}

	@Override
	public MolgenisUiMenu getMenu(String menuId)
	{
		MolgenisUiMenu menu = molgenisUi.getMenu(menuId);
		return menu != null ? new MolgenisUiMenuPermissionDecorator(menu, permissionService) : null;
	}

	public String getMenuJson()
	{
		return MenuUtils.getMenuJson(getMenu());
	}
}
