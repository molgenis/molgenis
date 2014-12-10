package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.UIParameterValueMetaData;
import org.molgenis.data.support.MapEntity;

public class UIParameterValue extends MapEntity
{
	private static final long serialVersionUID = -5971391998716286675L;

	public UIParameterValue()
	{
		super(UIParameterValueMetaData.IDENTIFIER);
	}

	public UIParameterValue(String identifier, UIParameter parameter, String value)
	{
		this();
		set(UIParameterValueMetaData.IDENTIFIER, identifier);
		set(UIParameterValueMetaData.PARAMETER, parameter);
		set(UIParameterValueMetaData.VALUE, value);
	}

	public String getIdentifier()
	{
		return getString(UIParameterValueMetaData.IDENTIFIER);
	}

	public UIParameter getParameter()
	{
		return getEntity(UIParameterValueMetaData.PARAMETER, UIParameter.class);
	}

	public String getValue()
	{
		return getString(UIParameterValueMetaData.VALUE);
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

		return getIdentifier().equals(((UIParameterValue) obj).getIdentifier());
	}
}
