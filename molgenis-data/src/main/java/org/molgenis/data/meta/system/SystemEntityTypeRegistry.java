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

	public SystemEntityType getSystemEntityType(String entityTypeId)
	{
		return systemEntityTypeMap.get(entityTypeId);
	}

	public Stream<SystemEntityType> getSystemEntityTypes()
	{
		return systemEntityTypeMap.values().stream();
	}

	public boolean hasSystemEntityType(String entityTypeId)
	{
		return systemEntityTypeMap.containsKey(entityTypeId);
	}

	void addSystemEntityType(SystemEntityType systemEntityType)
	{
		String systemEntityTypeName = systemEntityType.getId();
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
