package org.molgenis.data;

import java.io.Closeable;

/**
 * Repository that can be extended with more entity instances.
 */
public interface Writable extends Closeable
{
	/** Add one entity */
	void add(Entity entity);

	/** Stream add multiple entities */
	Integer add(Iterable<? extends Entity> entities);

	void flush();

	void clearCache();
}
