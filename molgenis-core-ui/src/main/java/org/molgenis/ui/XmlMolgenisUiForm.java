package org.molgenis.ui;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;

public class XmlMolgenisUiForm implements MolgenisUiMenuItem
{
	private final MolgenisPermissionService molgenisPermissionService;
	private final FormType formType;
	private final MolgenisUiMenu parentMenu;

	public XmlMolgenisUiForm(MolgenisPermissionService molgenisPermissionService, FormType formType,
			MolgenisUiMenu parentMenu)
	{
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenis permission service is null");
		if (formType == null) throw new IllegalArgumentException("form type is null");
		if (parentMenu == null) throw new IllegalArgumentException("parent menu is null");
		this.molgenisPermissionService = molgenisPermissionService;
		this.formType = formType;
		this.parentMenu = parentMenu;
	}

	@Override
	public String getId()
	{
		return formType.getName();
	}

	@Override
	public String getName()
	{
		String label = formType.getLabel();
		return label != null ? label : getId();
	}

	@Override
	public MolgenisUiMenuItemType getType()
	{
		return MolgenisUiMenuItemType.FORM;
	}

	@Override
	public boolean isAuthorized()
	{
		return molgenisPermissionService.hasPermissionOnEntity(formType.getEntity(), Permission.READ);
	}

	@Override
	public MolgenisUiMenu getParentMenu()
	{
		return parentMenu;
	}
}
