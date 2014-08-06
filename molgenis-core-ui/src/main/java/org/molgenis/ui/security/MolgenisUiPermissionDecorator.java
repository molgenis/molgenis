package org.molgenis.ui.security;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;

public class MolgenisUiPermissionDecorator implements MolgenisUi
{
	private final MolgenisUi molgenisUi;
	private final MolgenisPermissionService molgenisPermissionService;

	public MolgenisUiPermissionDecorator(MolgenisUi molgenisUi, MolgenisPermissionService molgenisPermissionService)
	{
		if (molgenisUi == null) throw new IllegalArgumentException("molgenisUi is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenisPermissionService is null");
		this.molgenisUi = molgenisUi;
		this.molgenisPermissionService = molgenisPermissionService;
	}

	@Override
	public String getTitle()
	{
		return molgenisUi.getTitle();
	}

	@Override
	public String getHrefLogo()
	{
		return molgenisUi.getHrefLogo();
	}

	@Override
	public String getHrefCss()
	{
		return molgenisUi.getHrefCss();
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
