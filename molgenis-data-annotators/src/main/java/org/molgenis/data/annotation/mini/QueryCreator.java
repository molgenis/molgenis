package org.molgenis.data.annotation.mini;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;

/**
 * Creates a query to send to the annotator's source repository to find rows to annotate with.
 */
public interface QueryCreator extends AnnotationStep
{
	Query createQuery(Entity entity);
}
