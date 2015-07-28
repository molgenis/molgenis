package org.molgenis.ui.form;

import org.molgenis.model.elements.Entity;

public class SubEntityForm extends EntityForm
{
	private final String xrefFieldName;

	public SubEntityForm(Entity entityMetaData, boolean hasWritePermission, String xrefFieldName)
	{
		super(entityMetaData, hasWritePermission);
		this.xrefFieldName = xrefFieldName;
	}

	public String getXrefFieldName()
	{
		return xrefFieldName;
	}

}
