package org.molgenis.ui;

import org.molgenis.framework.server.MolgenisPermissionService;

public class XmlMolgenisUiForm implements MolgenisUiMenuItem
{
	private final MolgenisPermissionService molgenisPermissionService;
	private final FormType formType;

	public XmlMolgenisUiForm(MolgenisPermissionService molgenisPermissionService, FormType formType)
	{
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenis permission service is null");
		if (formType == null) throw new IllegalArgumentException("form type is null");
		this.molgenisPermissionService = molgenisPermissionService;
		this.formType = formType;
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
		return molgenisPermissionService.hasReadPermissionOnEntity(formType.getEntity());
	}
}
