package org.molgenis.ui.security;

import org.molgenis.data.security.PermissionService;
import org.molgenis.ui.menu.MenuUtils;
import org.molgenis.web.Ui;
import org.molgenis.web.UiMenu;

import static java.util.Objects.requireNonNull;

public class MolgenisUiPermissionDecorator implements Ui
{
	private final Ui molgenisUi;
	private final PermissionService permissionService;

	public MolgenisUiPermissionDecorator(Ui molgenisUi, PermissionService permissionService)
	{
		this.molgenisUi = requireNonNull(molgenisUi);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public UiMenu getMenu()
	{
		UiMenu menu = molgenisUi.getMenu();
		return menu != null ? new MolgenisUiMenuPermissionDecorator(menu, permissionService) : null;
	}

	@Override
	public UiMenu getMenu(String menuId)
	{
		UiMenu menu = molgenisUi.getMenu(menuId);
		return menu != null ? new MolgenisUiMenuPermissionDecorator(menu, permissionService) : null;
	}

	public String getMenuJson()
	{
		return MenuUtils.getMenuJson(getMenu());
	}
}
