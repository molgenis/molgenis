package org.molgenis.omx.protocol;

import java.util.Collections;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
		return MolgenisFieldTypes.getType(FieldTypeEnum.COMPOUND.toString().toLowerCase());
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

	@Override
	public Iterable<AttributeMetaData> getAttributeParts()
	{
		Iterable<AttributeMetaData> allIterable = Collections.emptyList();

		List<Protocol> subprotocols = protocol.getSubprotocols();
		if (subprotocols != null)
		{
			Iterable<AttributeMetaData> protocolsIterable = Iterables.transform(subprotocols,
					new Function<Protocol, AttributeMetaData>()
					{

						@Override
						public AttributeMetaData apply(Protocol protocol)
						{
							return new ProtocolAttributeMetaData(protocol);
						}

					});
			allIterable = Iterables.concat(allIterable, protocolsIterable);
		}

		List<ObservableFeature> features = protocol.getFeatures();
		if (features != null)
		{
			Iterable<AttributeMetaData> featuresIterable = Iterables.transform(features,
					new Function<ObservableFeature, AttributeMetaData>()
					{
						@Override
						public AttributeMetaData apply(ObservableFeature observableFeature)
						{
							return new ObservableFeatureAttributeMetaData(observableFeature);
						}
					});
			allIterable = Iterables.concat(allIterable, featuresIterable);
		}

		return Lists.newArrayList(allIterable);
	}

	@Override
	public boolean isAuto()
	{
		return false;
	}

	@Override
	public boolean isLookupAttribute()
	{
		return false;
	}

	@Override
	public boolean isAggregateable()
	{
		return false;
	}

	@Override
	public Range getRange()
	{
		return null;
	}

	@Override
	public List<String> getEnumOptions()
	{
		return null;
	}

	@Override
	public boolean isSameAs(AttributeMetaData attributeMetaData)
	{
		return false;
	}
}
