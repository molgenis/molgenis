package org.molgenis.data.meta.system;

import com.google.common.collect.Maps;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.molgenis.data.meta.AttributeType.COMPOUND;

/**
 * Registry containing all {@link SystemEntityType}.
 */
@Component
public class SystemEntityTypeRegistry
{
	private final Logger LOG = LoggerFactory.getLogger(SystemEntityTypeRegistry.class);

	private final Map<String, SystemEntityType> systemEntityTypeMap;

	public SystemEntityTypeRegistry()
	{
		systemEntityTypeMap = Maps.newHashMap();
	}

	public SystemEntityType getSystemEntityType(String entityName)
	{
		return systemEntityTypeMap.get(entityName);
	}

	public Stream<SystemEntityType> getSystemEntityTypes()
	{
		return systemEntityTypeMap.values().stream();
	}

	public boolean hasSystemEntityType(String entityName)
	{
		return systemEntityTypeMap.containsKey(entityName);
	}

	void addSystemEntityType(SystemEntityType systemEntityType)
	{
		String systemEntityTypeName = systemEntityType.getFullyQualifiedName();
		if (systemEntityTypeName == null)
		{
			throw new IllegalArgumentException(
					format("[%s] is missing name, did you forget to call setFullyQualifiedName()?",
					systemEntityType.getClass().getSimpleName()));
		}

		LOG.trace("Registering system entity [{}] ...", systemEntityTypeName);
		systemEntityTypeMap.put(systemEntityTypeName, systemEntityType);
	}

	public boolean hasSystemAttribute(String attrIdentifier)
	{
		return getSystemAttribute(attrIdentifier) != null;
	}

	public Attribute getSystemAttribute(String attrIdentifier)
	{
		return getSystemEntityTypes().map(systemEntityType -> getSystemAttribute(systemEntityType, attrIdentifier))
				.filter(Objects::nonNull).findFirst().orElse(null);
	}

	private static Attribute getSystemAttribute(SystemEntityType systemEntityType, String attrIdentifier)
	{
		return getSystemAttributeRec(systemEntityType.getAllAttributes(), attrIdentifier);
	}

	private static Attribute getSystemAttributeRec(Iterable<Attribute> attrs, String attrIdentifier)
	{
		for (Attribute attr : attrs)
		{
			if (attr.getIdentifier() != null && attr.getIdentifier().equals(attrIdentifier))
			{
				return attr;
			}
			else
			{
				if (attr.getDataType() == COMPOUND)
				{
					return getSystemAttributeRec(attr.getChildren(), attrIdentifier);
				}
			}
		}
		return null;
	}
}
