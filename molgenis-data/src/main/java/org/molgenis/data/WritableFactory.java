package org.molgenis.data;

import java.io.Closeable;
import java.util.List;

import org.molgenis.data.support.AbstractWritable.AttributeWriteMode;

/**
 * Creates a new Entity Writable
 */
public interface WritableFactory extends Closeable
{
	Writable createWritable(String entityName, List<String> attributeNames);

	Writable createWritable(String entityName, Iterable<AttributeMetaData> attributes,
			AttributeWriteMode attributeWriteMode);
}
