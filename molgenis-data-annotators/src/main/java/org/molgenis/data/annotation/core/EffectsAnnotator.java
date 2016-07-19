package org.molgenis.data.annotation.core;

import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;

public class EffectsAnnotator extends RepositoryAnnotatorImpl
{
	public EffectsAnnotator(String NAME)
	{
		super(NAME);
	}

	public void init(EntityAnnotator entityAnnotator)
	{
		super.init(entityAnnotator);
	}
}
