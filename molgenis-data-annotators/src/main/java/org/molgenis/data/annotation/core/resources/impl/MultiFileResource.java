package org.molgenis.data.annotation.core.resources.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.annotation.core.resources.MultiResourceConfig;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.ResourceConfig;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public abstract class MultiFileResource implements Resource
{
	private final String name;
	private final Map<String, ResourceImpl> resources = new HashMap<>();
	private final MultiResourceConfig config;

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
			// Config may change so keep querying the MultiResourceConfig for the current File
			this.resources.put(key, new ResourceImpl(name + key, () ->
			{
				ResourceConfig resourceConfig = config.getConfigs().get(key);
				if (resourceConfig == null)
				{
					initializeResources();
					return null;
				}
				File file = resourceConfig.getFile();
				return file;
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

	private static Optional<Object> getFirstEqualsValueFor(String attributeName, Query<Entity> q)
	{
		return q.getRules()
				.stream()
				.filter(rule -> attributeName.equals(rule.getField()))
				.filter(rule -> rule.getOperator() == QueryRule.Operator.EQUALS)
				.findFirst()
				.map(QueryRule::getValue);
	}

	@Override
	public boolean isAvailable()
	{
		// initialize after autowiring is complete and resources is empty
		if (resources.isEmpty())
		{
			initializeResources();
		}
		return resources.values().stream().allMatch(ResourceImpl::isAvailable);
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
		return getFirstEqualsValueFor(VcfAttributes.CHROM, q).map(Object::toString)
															 .filter(resources::containsKey)
															 .map(resources::get)
															 .map(resource -> resource.findAll(q))
															 .orElse(new ArrayList<>());
	}
}
