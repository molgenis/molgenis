package org.molgenis.data.support;

import org.molgenis.data.Entity;

public interface ExpressionEvaluator
{
	Object evaluate(Entity entity);
}
