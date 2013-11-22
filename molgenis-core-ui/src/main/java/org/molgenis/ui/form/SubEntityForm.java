package org.molgenis.ui.form;

import org.molgenis.data.EntityMetaData;

public class SubEntityForm extends EntityForm
{
	private final String xrefFieldName;

	public SubEntityForm(EntityMetaData entityMetaData, boolean hasWritePermission, String xrefFieldName)
	{
		super(entityMetaData, hasWritePermission);
		this.xrefFieldName = xrefFieldName;
	}

	public String getXrefFieldName()
	{
		return xrefFieldName;
	}

}
