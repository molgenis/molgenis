package org.molgenis.data.meta.system;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;

import java.util.stream.Stream;

/**
 * Registry containing all {@link SystemEntityType}.
 */
public interface SystemEntityTypeRegistry
{
	SystemEntityType getSystemEntityType(String entityTypeId);

	Stream<SystemEntityType> getSystemEntityTypes();

	boolean hasSystemEntityType(String entityTypeId);

	void addSystemEntityType(SystemEntityType systemEntityType);

	boolean hasSystemAttribute(String attrIdentifier);

	Attribute getSystemAttribute(String attrIdentifier);
}
