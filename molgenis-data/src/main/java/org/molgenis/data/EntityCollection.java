package org.molgenis.data;

import java.util.stream.Stream;

public interface EntityCollection extends Iterable<Entity>
{
	Iterable<String> getAttributeNames();

	/**
	 * Returns whether this entity collection is lazy, i.e. all entities are references to entities (= lazy entities)
	 * 
	 * @return whether this entity collection is lazy
	 */
	public boolean isLazy();

	/**
	 * Returns entity collection as a stream
	 * 
	 * @return entity stream
	 */
	public Stream<Entity> asStream();
}
