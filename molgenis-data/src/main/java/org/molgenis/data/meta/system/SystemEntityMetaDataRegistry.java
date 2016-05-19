package org.molgenis.data.meta.system;

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

	public AttributeMetaData getSystemAttributeMetaData(String attrIdentifier)
	{
		throw new UnsupportedOperationException(); // FIXME implement
	}
}
