package org.molgenis.data.annotation.entity;

import org.molgenis.data.Entity;

import com.google.common.base.Optional;

public interface ResultFilter extends EntityProcessor
{
	Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity);
}
