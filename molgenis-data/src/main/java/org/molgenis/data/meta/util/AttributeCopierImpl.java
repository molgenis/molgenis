package org.molgenis.data.meta.util;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class AttributeCopierImpl implements AttributeCopier
{
	private final AttributeFactory attributeFactory;

	public AttributeCopierImpl(AttributeFactory attributeFactory)
	{
		this.attributeFactory = requireNonNull(attributeFactory);
	}

	@Override
	public Attribute copy(Attribute attribute)
	{
		return Attribute.newInstance(attribute, EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS, attributeFactory);
	}
}