package org.molgenis.data.annotation.core.resources.impl.emx;

import org.molgenis.data.annotation.core.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;

public abstract class EmxResourceImpl extends ResourceImpl
{
	public EmxResourceImpl(String variantResource, SingleResourceConfig singleResourceConfig)
	{
		super(variantResource, singleResourceConfig);
	}

	@Override
	public synchronized boolean isAvailable()
	{
		//FIXME: check attributes and entityTypeId
		return super.isAvailable();
	}
}
