package org.molgenis.ui.security;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;

import static java.util.Objects.requireNonNull;

public class MolgenisUiPermissionDecorator implements MolgenisUi
{
	private final MolgenisUi molgenisUi;
	private final MolgenisPermissionService molgenisPermissionService;

	public MolgenisUiPermissionDecorator(MolgenisUi molgenisUi, MolgenisPermissionService molgenisPermissionService)
	{
		this.molgenisUi = requireNonNull(molgenisUi);
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
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
