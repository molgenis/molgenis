package org.molgenis.data;

import java.io.Closeable;
import java.util.List;

/**
 * Creates a new Entity Writable
 */
public interface WritableFactory<E extends Entity> extends Closeable
{
	Writable<E> createWritable(String entityName, List<String> attributeNames);
}
