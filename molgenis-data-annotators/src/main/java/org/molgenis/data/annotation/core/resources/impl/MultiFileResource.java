package org.molgenis.data.annotation.core.resources.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.annotation.core.resources.MultiResourceConfig;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.ResourceConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class MultiFileResource implements Resource
{
	private final String name;
	private final Map<String, ResourceImpl> resources = new HashMap<>();
	private final MultiResourceConfig config;
	private static final Logger LOG = LoggerFactory.getLogger(MultiFileResource.class);

	public MultiFileResource(String name, MultiResourceConfig config)
	{
		this.name = name;
		this.config = config;
	}

	private void initializeResources()
	{
		this.resources.clear();

		for (Entry<String, ResourceConfig> chromConfig : config.getConfigs().entrySet())
		{
			final String key = chromConfig.getKey();
			this.resources.put(key, new ResourceImpl(name + key, new ResourceConfig()
			{
				// Config may change so keep querying the MultiResourceConfig for the current File
				@Override
				public File getFile()
				{
					ResourceConfig resourceConfig = config.getConfigs().get(key);
					if (resourceConfig == null)
					{
						initializeResources();
						return null;
					}
					File file = resourceConfig.getFile();
					return file;
				}

			})
			{
				@Override
				public RepositoryFactory getRepositoryFactory()
				{
					return MultiFileResource.this.getRepositoryFactory();
				}
			});
		}
	}

	private static Object getFirstEqualsValueFor(String attributeName, Query<Entity> q)
	{
		return q.getRules()
				.stream()
				.filter(rule -> attributeName.equals(rule.getField())
						&& rule.getOperator() == QueryRule.Operator.EQUALS)
				.findFirst()
				.get()
				.getValue();
	}

	@Override
	public boolean isAvailable()
	{
		// initialize after autowiring is complete and resources is empty
		if (resources.isEmpty())
		{
			initializeResources();
		}

		for (Resource chrom : resources.values())
		{
			if (!chrom.isAvailable())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Iterable<Entity> findAll(Query<Entity> q)
	{
		// initialize after autowiring is complete and resources is empty
		isAvailable();
		Object chromValue = getFirstEqualsValueFor(VcfAttributes.CHROM, q);
		Iterable<Entity> result = new ArrayList<Entity>();

		if (chromValue != null)
		{
			String chromStringValue = chromValue.toString();
			Resource resource = resources.get(chromStringValue);

			try
			{
				result = resource.findAll(q);
			}
			catch (NullPointerException e)
			{
				LOG.debug("No file for chromosome %s skipping..", chromStringValue);
			}
		}

		return result;
	}
}
