package org.molgenis.data;

import java.io.Closeable;

/**
 * Repository that can be extended with more entity instances.
 */
public interface Writable<E extends Entity> extends Closeable
{
	/** Add one entity */
	void add(E entity);

	/** Stream add multiple entities */
	void add(Iterable<E> entities);

	void flush();

	void clearCache();
}
