package org.molgenis.data.meta.system;

import com.google.common.collect.Maps;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;

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
		systemEntityMetaDataMap = Maps.newHashMap();
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

	public Attribute getSystemAttributeMetaData(String attrIdentifier)
	{
		return getSystemEntityMetaDatas()
				.map(systemEntityMetaData -> getSystemAttributeMetaData(systemEntityMetaData, attrIdentifier))
				.filter(Objects::nonNull).findFirst().orElse(null);
	}

	private static Attribute getSystemAttributeMetaData(SystemEntityMetaData systemEntityMetaData,
			String attrIdentifier)
	{
		return getSystemAttributeMetaDataRec(systemEntityMetaData.getAllAttributes(), attrIdentifier);
	}

	private static Attribute getSystemAttributeMetaDataRec(Iterable<Attribute> attrs,
			String attrIdentifier)
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
					return getSystemAttributeMetaDataRec(attr.getAttributeParts(), attrIdentifier);
				}
			}
		}
		return null;
	}
}
