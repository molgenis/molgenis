package org.molgenis.data;

import java.io.Closeable;
import java.util.stream.Stream;

/**
 * Repository that can be extended with more entity instances.
 */
public interface Writable extends Closeable
{
	/**
	 * Add one entity
	 */
	void add(Entity entity);

	/**
	 * Stream add multiple entities
	 */
	Integer add(Stream<? extends Entity> entities);

	void flush();

	void clearCache();
}
