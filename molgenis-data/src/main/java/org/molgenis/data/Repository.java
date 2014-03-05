package org.molgenis.data;

import java.io.Closeable;

/**
 * Repository gives access to a collection of Entity. Synonyms: EntityReader, EntitySource, EntityCollection
 */
public interface Repository extends Iterable<Entity>, Closeable
{
	String getName();

	EntityMetaData getEntityMetaData();

	Class<? extends Entity> getEntityClass();

	<E extends Entity> Iterable<E> iterator(Class<E> clazz);

	String getUrl();
}
