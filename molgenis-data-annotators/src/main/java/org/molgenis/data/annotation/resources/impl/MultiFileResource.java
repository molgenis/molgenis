package org.molgenis.data.annotation.resources.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.annotation.resources.MultiResourceConfig;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charbonb on 15/06/15.
 */
public class MultiFileResource implements Resource
{
	private static final Logger LOG = LoggerFactory.getLogger(MultiFileResource.class);
	private final String name;
	private final Map<String, ResourceImpl> resources = new HashMap<>();
	private final MultiResourceConfig config;
	private final RepositoryFactory factory;

	public MultiFileResource(String name, MultiResourceConfig config, RepositoryFactory factory)
	{
		this.name = name;
		this.config = config;
		this.factory = factory;
	}

	private void initializeResources()
	{
		this.resources.clear();
		this.config.refreshConfigs();
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

			}, factory));
		}
		LOG.debug("initializeResources() --- initialize resources");
	}

	private static Object getFirstEqualsValueFor(String attributeName, Query q)
	{
		return q.getRules()
				.stream()
				.filter(rule -> attributeName.equals(rule.getField())
						&& rule.getOperator() == QueryRule.Operator.EQUALS).findFirst().get().getValue();
	}

	@Override
	public boolean isAvailable()
	{
		LOG.debug("isAvailable() --- start is available");
		// initialize after autowiring is complete and resources is empty
		if (resources.isEmpty())
		{
			initializeResources();
		}

		// Check if the configuration are changed
		if (!config.getConfigs().entrySet().equals(resources.entrySet()))
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
	public Iterable<Entity> findAll(Query q)
	{
		// initialize after autowiring is complete and resources is empty
		isAvailable();
		String chromValue = getFirstEqualsValueFor(VcfRepository.CHROM, q).toString();
		Resource resource = resources.get(chromValue);
		return resource.findAll(q);
	}

}
