package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.UIParameterValueMetaData;
import org.molgenis.compute.ui.meta.UIWorkflowParameterValueMetaData;
import org.molgenis.data.support.MapEntity;

public class UIWorkflowParameterValue extends MapEntity
{
	private static final long serialVersionUID = -5971391998716286675L;

	public UIWorkflowParameterValue()
	{
		super(UIParameterValueMetaData.IDENTIFIER);
	}

	public UIWorkflowParameterValue(String identifier, String value)
	{
		this();
		set(UIParameterValueMetaData.IDENTIFIER, identifier);
		set(UIParameterValueMetaData.VALUE, value);
	}

	public String getIdentifier()
	{
		return getString(UIWorkflowParameterValueMetaData.IDENTIFIER);
	}


	public String getValue()
	{
		return getString(UIWorkflowParameterValueMetaData.VALUE);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdentifier().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		return getIdentifier().equals(((UIWorkflowParameterValue) obj).getIdentifier());
	}
}
