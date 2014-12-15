package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.UIParameterMappingMetaData;
import org.molgenis.data.support.MapEntity;

public class UIParameterMapping extends MapEntity
{
	private static final long serialVersionUID = -8755713700085488910L;

	public UIParameterMapping()
	{
		super(UIParameterMappingMetaData.IDENTIFIER);
	}

	public UIParameterMapping(String identifier)
	{
		this();
		set(UIParameterMappingMetaData.IDENTIFIER, identifier);
	}

	public UIParameterMapping(String identifier, String from, String to)
	{
		this(identifier);
		setFrom(from);
		setTo(to);
	}

	public String getIdentifier()
	{
		return getString(UIParameterMappingMetaData.IDENTIFIER);
	}

	public String getFrom()
	{
		return getString(UIParameterMappingMetaData.FROM);
	}

	public void setFrom(String from)
	{
		set(UIParameterMappingMetaData.FROM, from);
	}

	public String getTo()
	{
		return getString(UIParameterMappingMetaData.TO);
	}

	public void setTo(String to)
	{
		set(UIParameterMappingMetaData.TO, to);
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

		return getIdentifier().equals(((UIParameterMapping) obj).getIdentifier());
	}
}
