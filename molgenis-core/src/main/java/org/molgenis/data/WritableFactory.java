package org.molgenis.data;

import java.io.Closeable;
import java.util.List;

/**
 * Creates a new Entity Writable
 */
public interface WritableFactory extends Closeable
{
	Writable createWritable(String entityName, List<String> attributeNames);
}
