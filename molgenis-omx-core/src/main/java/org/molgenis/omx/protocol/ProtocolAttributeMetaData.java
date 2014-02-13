package org.molgenis.omx.protocol;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.omx.observ.Protocol;

public class ProtocolAttributeMetaData implements AttributeMetaData
{
	private final Protocol protocol;

	public ProtocolAttributeMetaData(Protocol protocol)
	{
		if (protocol == null) throw new IllegalArgumentException("Protocol is null");
		this.protocol = protocol;
	}

	@Override
	public String getName()
	{
		return protocol.getIdentifier(); // yes, getIdentifier and not getName
	}

	@Override
	public String getLabel()
	{
		return protocol.getName(); // yes, getName
	}

	@Override
	public String getDescription()
	{
		return protocol.getDescription();
	}

	@Override
	public FieldType getDataType()
	{
		return MolgenisFieldTypes.getType(FieldTypeEnum.HAS.toString().toLowerCase());
	}

	@Override
	public boolean isNillable()
	{
		return true;
	}

	@Override
	public boolean isReadonly()
	{
		return false;
	}

	@Override
	public boolean isUnique()
	{
		return false;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

	@Override
	public Object getDefaultValue()
	{
		return null;
	}

	@Override
	public boolean isIdAtrribute()
	{
		return false;
	}

	@Override
	public boolean isLabelAttribute()
	{
		return false;
	}

	@Override
	public EntityMetaData getRefEntity()
	{
		return new ProtocolEntityMetaData(protocol);
	}
}
