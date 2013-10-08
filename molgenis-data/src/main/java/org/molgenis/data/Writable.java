package org.molgenis.data;

/**
 * Repository that can be extended with more entity instances.
 */
public interface Writable
{
	/** Add one entity */
	void add(Entity entity);

	/** Stream add multiple entities */
	void add(Iterable<? extends Entity> entities);
}
