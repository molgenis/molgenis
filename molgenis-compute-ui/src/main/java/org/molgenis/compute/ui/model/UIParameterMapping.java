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

	public UIParameterMapping(String identifier, UIParameter from, UIParameter to)
	{
		this(identifier);
		setFrom(from);
		setTo(to);
	}

	public String getIdentifier()
	{
		return getString(UIParameterMappingMetaData.IDENTIFIER);
	}

	public UIParameter getFrom()
	{
		return getEntity(UIParameterMappingMetaData.FROM, UIParameter.class);
	}

	public void setFrom(UIParameter parameter)
	{
		set(UIParameterMappingMetaData.FROM, parameter);
	}

	public UIParameter getTo()
	{
		return getEntity(UIParameterMappingMetaData.TO, UIParameter.class);
	}

	public void setTo(UIParameter to)
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
