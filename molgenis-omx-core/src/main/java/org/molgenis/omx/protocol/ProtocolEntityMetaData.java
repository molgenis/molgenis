package org.molgenis.omx.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.utils.ProtocolUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ProtocolEntityMetaData implements EntityMetaData
{
	private final Protocol protocol;

	public ProtocolEntityMetaData(Protocol protocol)
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
	public Iterable<AttributeMetaData> getAttributes()
	{
		List<Protocol> protocols = ProtocolUtils.getProtocolDescendants(protocol, true);

		Iterable<AttributeMetaData> attributeMetaDataIterable = new ArrayList<AttributeMetaData>();
		for (Protocol protocol : protocols)
		{
			Iterable<AttributeMetaData> attributeMetaData = Iterables.transform(protocol.getFeatures(),
					new Function<ObservableFeature, AttributeMetaData>()
					{
						@Override
						public AttributeMetaData apply(ObservableFeature observableFeature)
						{
							return new ObservableFeatureAttributeMetaData(observableFeature);
						}
					});
			attributeMetaDataIterable = Iterables.concat(attributeMetaDataIterable, attributeMetaData);
		}
		return attributeMetaDataIterable;
	}

	@Override
	public Iterable<AttributeMetaData> getLevelOneAttributes()
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
	public AttributeMetaData getIdAttribute()
	{
		for (AttributeMetaData attribute : getAttributes())
		{
			if (attribute.isIdAtrribute())
			{
				return attribute;
			}
		}

		return null;
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		for (AttributeMetaData attribute : getAttributes())
		{
			if (attribute.isLabelAttribute())
			{
				return attribute;
			}
		}

		return null;
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		for (AttributeMetaData attribute : getAttributes())
		{
			if (attribute.getName().equals(attributeName))
			{
				return attribute;
			}
		}

		return null;
	}
}
