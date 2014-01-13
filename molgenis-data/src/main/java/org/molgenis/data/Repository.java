package org.molgenis.data;

import java.io.Closeable;

/**
 * Repository gives access to a collection of Entity. Synonyms: EntityReader, EntitySource, EntityCollection
 */
public interface Repository<E extends Entity> extends EntityMetaData, Iterable<E>, Closeable
{
	Class<? extends Entity> getEntityClass();

	EntityMetaData getEntityMetaData();
}
