package org.molgenis.omx.protocol;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.AbstractEntityMetaData;
import org.molgenis.omx.observ.CategoryMetaData;
import org.molgenis.omx.observ.ObservableFeature;

public class OmxLookupTableEntityMetaData extends AbstractEntityMetaData
{
	private final ObservableFeature categoricalFeature;

	public OmxLookupTableEntityMetaData(ObservableFeature categoricalFeature)
	{
		if (categoricalFeature == null) throw new IllegalArgumentException("categoricalFeature is null");
		this.categoricalFeature = categoricalFeature;
	}

	@Override
	public String getName()
	{
		return categoricalFeature.getIdentifier() + "-LUT"; // yes, Identifier
	}

	@Override
	public String getLabel()
	{
		return categoricalFeature.getName() + " lookup table"; // yes, Name
	}

	@Override
	public String getDescription()
	{
		return "Lookup table for: " + categoricalFeature.getDescription();
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		return new CategoryMetaData().getAttributes();
	}
}
