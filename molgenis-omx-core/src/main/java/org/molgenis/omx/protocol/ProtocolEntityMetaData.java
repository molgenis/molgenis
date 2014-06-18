package org.molgenis.omx.protocol;

import java.util.Arrays;
import java.util.Collections;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractEntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class ProtocolEntityMetaData extends AbstractEntityMetaData
{
	private final Protocol protocol;
	private transient Iterable<AttributeMetaData> cachedAttributesMetaData;

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
	public boolean isAbstract()
	{
		return false;
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
		if (cachedAttributesMetaData == null)
		{
			// get compound attributes (subprotocols)
			Iterable<AttributeMetaData> subprotocolsAttributeMetaData;
			Iterable<Protocol> subprotocols = protocol.getSubprotocols();
			if (subprotocols != null)
			{
				subprotocolsAttributeMetaData = Iterables.transform(subprotocols,
						new Function<Protocol, AttributeMetaData>()
						{
							@Override
							public AttributeMetaData apply(Protocol protocol)
							{
								return new ProtocolAttributeMetaData(protocol);
							}

						});
			}
			else subprotocolsAttributeMetaData = Collections.emptyList();

			// get non-compound attributes (observable features)
			Iterable<AttributeMetaData> observableFeaturesAttributeMetaData;
			Iterable<ObservableFeature> observableFeatures = protocol.getFeatures();
			if (observableFeatures != null)
			{
				observableFeaturesAttributeMetaData = Iterables.transform(protocol.getFeatures(),
						new Function<ObservableFeature, AttributeMetaData>()
						{
							@Override
							public AttributeMetaData apply(ObservableFeature observableFeature)
							{
								return new ObservableFeatureAttributeMetaData(observableFeature);
							}
						});
			}
			else observableFeaturesAttributeMetaData = Collections.emptyList();

			DefaultAttributeMetaData idAttr = new DefaultAttributeMetaData("observationsetid", FieldTypeEnum.LONG)
					.setIdAttribute(true).setUnique(true).setVisible(false);
			idAttr.setReadOnly(true);

			cachedAttributesMetaData = Iterables.concat(subprotocolsAttributeMetaData,
					observableFeaturesAttributeMetaData, Arrays.asList(idAttr));

		}
		return cachedAttributesMetaData;
	}

	@Override
	public EntityMetaData getExtends()
	{
		return null;
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
	}
}
