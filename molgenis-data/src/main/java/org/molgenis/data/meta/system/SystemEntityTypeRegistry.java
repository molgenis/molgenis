package org.molgenis.data.meta.system;

import com.google.common.collect.Maps;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;

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

	public void addSystemEntityType(SystemEntityType systemEntityType)
	{
		String systemEntityTypeName = systemEntityType.getName();
		if (systemEntityTypeName == null)
		{
			throw new IllegalArgumentException(format("[%s] is missing name, did you forget to call setName()?",
					systemEntityType.getClass().getSimpleName()));
		}

		LOG.trace("Registering system entity [{}] ...", systemEntityTypeName);
		systemEntityTypeMap.put(systemEntityTypeName, systemEntityType);
	}

	public boolean hasSystemAttributeMetaData(String attrIdentifier)
	{
		return getSystemAttributeMetaData(attrIdentifier) != null;
	}

	public AttributeMetaData getSystemAttributeMetaData(String attrIdentifier)
	{
		return getSystemEntityTypes()
				.map(systemEntityType -> getSystemAttributeMetaData(systemEntityType, attrIdentifier))
				.filter(Objects::nonNull).findFirst().orElse(null);
	}

	private static AttributeMetaData getSystemAttributeMetaData(SystemEntityType systemEntityType,
			String attrIdentifier)
	{
		return getSystemAttributeMetaDataRec(systemEntityType.getAllAttributes(), attrIdentifier);
	}

	private static AttributeMetaData getSystemAttributeMetaDataRec(Iterable<AttributeMetaData> attrs,
			String attrIdentifier)
	{
		for (AttributeMetaData attr : attrs)
		{
			if (attr.getIdentifier() != null && attr.getIdentifier().equals(attrIdentifier))
			{
				return attr;
			}
			else
			{
				if (attr.getDataType() == COMPOUND)
				{
					return getSystemAttributeMetaDataRec(attr.getAttributeParts(), attrIdentifier);
				}
			}
		}
		return null;
	}
}
