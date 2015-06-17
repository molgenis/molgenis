package org.molgenis.data.annotation.resources.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.annotation.resources.MultiResourceConfig;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.data.vcf.VcfRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by charbonb on 15/06/15.
 */
public class MultiFileResource implements Resource
{
	private String name;
	private Map<String, Resource> resources;

	public MultiFileResource(String name, MultiResourceConfig config, EntityMetaData emd)
	{
		this.name = name;
		config.getConfigs();
		Map<String, Resource> resources = new HashMap<>();
		Map<String, ResourceConfig> configs = config.getConfigs();
		for (String chrom : configs.keySet())
		{
			resources.put(chrom, new ResourceImpl(name + chrom, configs.get(chrom), new TabixRepositoryFactory(emd)));
		}

		this.resources = resources;
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
		for (String chrom : resources.keySet())
		{
			if (resources.get(chrom).isAvailable() == false)
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
		String chromValue = getFirstEqualsValueFor(VcfRepository.CHROM, q).toString();
		Resource resource = resources.get(chromValue);
		return resource.findAll(q);
	}

	@Override
	public boolean needsRefresh()
	{
		for(Resource resource : resources.values()){
			if(resource.needsRefresh() == true){
				return true;
			}
		}
		return false;
	}

}
