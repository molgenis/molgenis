package org.molgenis.data.meta.system;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;

import java.util.Objects;
import java.util.stream.Stream;

import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

/**
 * Registry containing all {@link SystemEntityMetaData}.
 *
 * @see SystemEntityMetaDataRegistrySingleton
 */
@Component
public class SystemEntityMetaDataRegistry
{
	public SystemEntityMetaData getSystemEntityMetaData(String entityName)
	{
		return SystemEntityMetaDataRegistrySingleton.INSTANCE.getSystemEntityMetaData(entityName);
	}

	public Stream<SystemEntityMetaData> getSystemEntityMetaDatas()
	{
		return SystemEntityMetaDataRegistrySingleton.INSTANCE.getSystemEntityMetaDatas();
	}

	public boolean hasSystemEntityMetaData(String entityName)
	{
		return SystemEntityMetaDataRegistrySingleton.INSTANCE.hasSystemEntityMetaData(entityName);
	}

	public void addSystemEntityMetaData(SystemEntityMetaData systemEntityMetaData)
	{
		SystemEntityMetaDataRegistrySingleton.INSTANCE.addSystemEntityMetaData(systemEntityMetaData);
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
			if (attr.getIdentifier() == null)
			{
				continue; // FIXME this happens for EntityMetaDataMetaData i18n attrs
			}

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
