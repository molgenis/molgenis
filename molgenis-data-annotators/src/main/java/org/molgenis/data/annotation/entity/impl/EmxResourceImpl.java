package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.annotation.resources.impl.InMemoryRepositoryFactory;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;

import java.util.List;

public class EmxResourceImpl extends ResourceImpl
{
	public EmxResourceImpl(String variantResource, SingleResourceConfig singleResourceConfig,
			InMemoryRepositoryFactory filename, String entityName, List<String> attributeNames)
	{
		super(variantResource, singleResourceConfig, filename);
	}

    @Override
    public synchronized boolean isAvailable()
    {
        //FIXME: check attributes and entityName
        return super.isAvailable();
    }
}
