package org.molgenis.data.annotation.core.entity;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;

/**
 * Creates a query to send to the annotator's source repository to find rows to annotate with.
 */
public interface QueryCreator extends EntityProcessor
{
	Query<Entity> createQuery(Entity entity);
}
