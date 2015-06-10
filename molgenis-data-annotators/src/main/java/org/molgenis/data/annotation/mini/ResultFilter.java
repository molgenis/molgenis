package org.molgenis.data.annotation.mini;

import org.molgenis.data.Entity;

import com.google.common.base.Optional;

public interface ResultFilter extends AnnotationStep
{
	Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity);
}
