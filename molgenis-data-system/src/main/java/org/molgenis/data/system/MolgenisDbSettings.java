package org.molgenis.data.system;

import java.util.Map;
import java.util.TreeMap;

import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.system.core.RuntimeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "We want to return Boolean.TRUE, Boolean.FALSE or null")
public class MolgenisDbSettings implements MolgenisSettings
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisDbSettings.class);

	private final DataService dataService;

	@Autowired
	public MolgenisDbSettings(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	@RunAsSystem
	public String getProperty(String name)
	{
		return getProperty(name, null);
	}

	@Override
	@RunAsSystem
	public String getProperty(String name, String defaultValue)
	{
		Query propertyRule = new QueryImpl().eq(RuntimeProperty.NAME, name);

		RuntimeProperty property;
		try
		{
			property = dataService.findOne(RuntimeProperty.ENTITY_NAME, propertyRule, RuntimeProperty.class);
		}
		catch (MolgenisDataException e)
		{
			LOG.warn("", e);
			return defaultValue;
		}

		if (property == null)
		{
			LOG.debug(RuntimeProperty.class.getSimpleName() + " '" + name + "' is null");
			return defaultValue;
		}

		return property.getValue();
	}

	@Override
	public void setProperty(String name, String value)
	{
		RuntimeProperty property = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				new QueryImpl().eq(RuntimeProperty.NAME, name), RuntimeProperty.class);

		if (property == null)
		{
			property = new RuntimeProperty();
			property.setName(name);
			property.setValue(value);
			dataService.add(RuntimeProperty.ENTITY_NAME, property);
		}
		else
		{
			property.setValue(value);
			dataService.update(RuntimeProperty.ENTITY_NAME, property);
		}
	}

	@Override
	@RunAsSystem
	public Boolean getBooleanProperty(String key)
	{
		String value = getProperty(key);
		if (value == null)
		{
			return null;
		}

		return Boolean.valueOf(value);
	}

	@Override
	@RunAsSystem
	public boolean getBooleanProperty(String key, boolean defaultValue)
	{
		Boolean value = getBooleanProperty(key);
		if (value == null)
		{
			return defaultValue;
		}

		return value;
	}

	@Override
	public boolean updateProperty(String name, String value)
	{
		if (null == value)
		{
			throw new IllegalArgumentException("content is null");
		}

		Query query = new QueryImpl().eq(RuntimeProperty.NAME, name);
		try
		{
			RuntimeProperty property = dataService.findOne(RuntimeProperty.ENTITY_NAME, query, RuntimeProperty.class);
			if (property != null)
			{
				property.setValue(value);
				dataService.update(RuntimeProperty.ENTITY_NAME, property);
				return true;
			}
		}
		catch (MolgenisDataException e)
		{
			LOG.warn("", e);
		}

		return false;
	}

	@Override
	@RunAsSystem
	public boolean propertyExists(String name)
	{
		long count = dataService.count(RuntimeProperty.ENTITY_NAME, new QueryImpl().eq(RuntimeProperty.NAME, name));
		if (count > 0)
		{
			return true;
		}

		return false;
	}

	@Override
	@RunAsSystem
	public Map<String, String> getProperties(String prefix)
	{

		Iterable<RuntimeProperty> properties = dataService.findAll(RuntimeProperty.ENTITY_NAME, RuntimeProperty.class);
		Map<String, String> result = new TreeMap<String, String>();
		for (RuntimeProperty property : properties)
		{
			if (property.getName().startsWith(prefix))
			{
				result.put(property.getName().substring(prefix.length() + 1), property.getValue());
			}
		}
		return result;
	}

	@Override
	@RunAsSystem
	public Integer getIntegerProperty(String key)
	{
		String value = getProperty(key);
		if (value == null)
		{
			return null;
		}

		return DataConverter.toInt(value);
	}
}
