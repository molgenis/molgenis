package org.molgenis.omx;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.util.WebAppUtil;

public class MolgenisDbSettings implements MolgenisSettings
{
	private static final Logger logger = Logger.getLogger(MolgenisDbSettings.class);

	@Override
	public String getProperty(String key)
	{
		return getProperty(key, null);
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		QueryRule propertyRule = new QueryRule(RuntimeProperty.IDENTIFIER, Operator.EQUALS,
				RuntimeProperty.class.getSimpleName() + '_' + key);
		List<RuntimeProperty> properties;
		try
		{
			properties = WebAppUtil.getDatabase().find(RuntimeProperty.class, propertyRule);
		}
		catch (DatabaseException e)
		{
			logger.warn(e);
			return defaultValue;
		}
		if (properties == null || properties.isEmpty()) return defaultValue;
		RuntimeProperty property = properties.get(0);
		if (property == null)
		{
			logger.warn(RuntimeProperty.class.getSimpleName() + " '" + key + "' is null");
			return defaultValue;
		}
		return property.getValue();
	}

	@Override
	public void setProperty(String key, String value)
	{
		RuntimeProperty property = new RuntimeProperty();
		property.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + key);
		property.setName(key);
		property.setValue(value);
		try
		{
			WebAppUtil.getDatabase().add(property);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}
}
