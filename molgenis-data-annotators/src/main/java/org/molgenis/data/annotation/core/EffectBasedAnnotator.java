package org.molgenis.data.annotation.core;

import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;

/**
 * Annotator that annotates the "effects" entities rather than the variants themself
 */
public class EffectBasedAnnotator extends RepositoryAnnotatorImpl
{
	public EffectBasedAnnotator(String NAME)
	{
		super(NAME);
	}

	public void init(EntityAnnotator entityAnnotator)
	{
		super.init(entityAnnotator);
	}
}
