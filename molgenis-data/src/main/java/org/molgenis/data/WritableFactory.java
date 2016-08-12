package org.molgenis.data;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.support.AbstractWritable.AttributeWriteMode;

import java.io.Closeable;
import java.util.List;

/**
 * Creates a new Entity Writable
 */
public interface WritableFactory extends Closeable
{
	Writable createWritable(String entityName, List<String> attributeNames);

	Writable createWritable(String entityName, Iterable<AttributeMetaData> attributes,
			AttributeWriteMode attributeWriteMode);
}
