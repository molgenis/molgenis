package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.UIParameterMetaData;
import org.molgenis.data.support.MapEntity;

public class UIParameter extends MapEntity
{
	private static final long serialVersionUID = -4006185977776378334L;

	public UIParameter()
	{
		super(UIParameterMetaData.IDENTIFIER);
	}

	public UIParameter(String identifier, String name)
	{
		this();
		set(UIParameterMetaData.IDENTIFIER, identifier);
		set(UIParameterMetaData.NAME, name);
	}

	public String getIdentifier()
	{
		return getString(UIParameterMetaData.IDENTIFIER);
	}

	public String getName()
	{
		return getString(UIParameterMetaData.NAME);
	}

	public void setName(String name)
	{
		set(UIParameterMetaData.NAME, name);
	}

	public void setType(ParameterType type)
	{
		set(UIParameterMetaData.TYPE, type.toString());
	}

	public ParameterType getType()
	{
		String typeStr = getString(UIParameterMetaData.TYPE);
		if (typeStr == null) return null;

		return ParameterType.valueOf(typeStr);
	}

	public String getDataType()
	{
		return getString(UIParameterMetaData.DATA_TYPE);
	}

	public void setDataType(String dataType)
	{
		set(UIParameterMetaData.DATA_TYPE, dataType);
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

		return getIdentifier().equals(((UIParameter) obj).getIdentifier());
	}
}
