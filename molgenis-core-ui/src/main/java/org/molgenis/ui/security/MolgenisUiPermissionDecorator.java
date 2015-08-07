package org.molgenis.ui.security;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;

public class MolgenisUiPermissionDecorator implements MolgenisUi
{
	private final MolgenisUi molgenisUi;
	private final MolgenisPermissionService molgenisPermissionService;

	public MolgenisUiPermissionDecorator(MolgenisUi molgenisUi, MolgenisPermissionService molgenisPermissionService)
	{
		this.molgenisUi = checkNotNull(molgenisUi);
		this.molgenisPermissionService = checkNotNull(molgenisPermissionService);
	}

	@Override
	public MolgenisUiMenu getMenu()
	{
		MolgenisUiMenu menu = molgenisUi.getMenu();
		return menu != null ? new MolgenisUiMenuPermissionDecorator(menu, molgenisPermissionService) : null;
	}

	@Override
	public MolgenisUiMenu getMenu(String menuId)
	{
		MolgenisUiMenu menu = molgenisUi.getMenu(menuId);
		return menu != null ? new MolgenisUiMenuPermissionDecorator(menu, molgenisPermissionService) : null;
	}
}
