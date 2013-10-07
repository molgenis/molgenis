package org.molgenis.data;

/**
 * Repository gives access to a collection of Entity. Synonyms: EntityReader, EntitySource, EntityCollection
 */
public interface Repository<E extends Entity> extends EntityMetaData, Iterable<E>
{
}
