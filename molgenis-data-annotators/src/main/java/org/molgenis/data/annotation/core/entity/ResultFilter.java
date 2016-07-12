package org.molgenis.data.annotation.core.entity;

import com.google.common.base.Optional;
import org.molgenis.data.Entity;

public interface ResultFilter extends EntityProcessor
{
	Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity, boolean updateMode);
}
