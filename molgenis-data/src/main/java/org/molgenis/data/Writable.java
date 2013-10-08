package org.molgenis.data;

/**
 * Repository that can be extended with more entity instances.
 */
public interface Writable<E extends Entity>
{
	/** Add one entity */
	void add(E entity);

	/** Stream add multiple entities */
	void add(Iterable<E> entities);
}
