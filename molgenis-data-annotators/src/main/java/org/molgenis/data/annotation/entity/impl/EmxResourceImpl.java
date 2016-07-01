package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;

public abstract class EmxResourceImpl extends ResourceImpl
{
	public EmxResourceImpl(String variantResource, SingleResourceConfig singleResourceConfig)
	{
		super(variantResource, singleResourceConfig);
	}

    @Override
    public synchronized boolean isAvailable()
    {
        //FIXME: check attributes and entityName
        return super.isAvailable();
    }
}
