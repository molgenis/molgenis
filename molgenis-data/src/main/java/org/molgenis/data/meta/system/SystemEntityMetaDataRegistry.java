package org.molgenis.data.meta.system;

import static java.lang.String.format;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registry containing all {@link SystemEntityMetaData}.
 */
@Component
public class SystemEntityMetaDataRegistry
{
	private final Logger LOG = LoggerFactory.getLogger(SystemEntityMetaDataRegistry.class);

	private final Map<String, SystemEntityMetaData> systemEntityMetaDataMap;

	public SystemEntityMetaDataRegistry()
	{
		systemEntityMetaDataMap = new HashMap<>();
	}

	public SystemEntityMetaData getSystemEntityMetaData(String entityName)
	{
		return systemEntityMetaDataMap.get(entityName);
	}

	public Stream<SystemEntityMetaData> getSystemEntityMetaDatas()
	{
		return systemEntityMetaDataMap.values().stream();
	}

	public boolean hasSystemEntityMetaData(String entityName)
	{
		return systemEntityMetaDataMap.containsKey(entityName);
	}

	public void addSystemEntityMetaData(SystemEntityMetaData systemEntityMetaData)
	{
		String systemEntityMetaDataName = systemEntityMetaData.getName();
		if (systemEntityMetaDataName == null)
		{
			throw new IllegalArgumentException(format("[%s] is missing name, did you forget to call setName()?",
					systemEntityMetaData.getClass().getSimpleName()));
		}

		LOG.trace("Registering system entity [{}] ...", systemEntityMetaDataName);
		systemEntityMetaDataMap.put(systemEntityMetaDataName, systemEntityMetaData);
	}

	public boolean hasSystemAttributeMetaData(String attrIdentifier)
	{
		return getSystemAttributeMetaData(attrIdentifier) != null;
	}

	public AttributeMetaData getSystemAttributeMetaData(String attrIdentifier)
	{
		return getSystemEntityMetaDatas()
				.map(systemEntityMetaData -> getSystemAttributeMetaData(systemEntityMetaData, attrIdentifier))
				.filter(Objects::nonNull).findFirst().orElse(null);
	}

	private AttributeMetaData getSystemAttributeMetaData(SystemEntityMetaData systemEntityMetaData,
			String attrIdentifier)
	{
		return getSystemAttributeMetaDataRec(systemEntityMetaData.getAllAttributes(), attrIdentifier);
	}

	private AttributeMetaData getSystemAttributeMetaDataRec(Iterable<AttributeMetaData> attrs, String attrIdentifier)
	{
		for (AttributeMetaData attr : attrs)
		{
			if (attr.getIdentifier().equals(attrIdentifier))
			{
				return attr;
			}
			else
			{
				if (attr.getDataType().getEnumType() == COMPOUND)
				{
					return getSystemAttributeMetaDataRec(attr.getAttributeParts(), attrIdentifier);
				}
			}
		}
		return null;
	}
}
