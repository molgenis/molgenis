package org.molgenis.data;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface EntityCollection extends Iterable<Entity>
{
	/**
	 * Streams the {@link Entity}s
	 */
	default Stream<Entity> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}

	Iterable<String> getAttributeNames();

	/**
	 * Returns whether this entity collection is lazy, i.e. all entities are references to entities (= lazy entities)
	 *
	 * @return whether this entity collection is lazy
	 */
	boolean isLazy();

}
