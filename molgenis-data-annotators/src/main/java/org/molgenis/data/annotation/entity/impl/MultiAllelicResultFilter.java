package org.molgenis.data.annotation.entity.impl;

import com.google.common.collect.FluentIterable;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.vcf.VcfRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO before merging with the master this should be unit-tested
public class MultiAllelicResultFilter implements ResultFilter
{

	private List<AttributeMetaData> attributes;
	public MultiAllelicResultFilter(List<AttributeMetaData> attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(VcfRepository.REF_META, VcfRepository.ALT_META);
	}

	@Override
	public com.google.common.base.Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
	{
		Map<String, String> alleleValueMap = new HashMap<>();
		List<Entity> processedResults = new ArrayList<>();

		for (Entity entity : results)
		{
			if (entity.get(VcfRepository.REF).equals(annotatedEntity.get(VcfRepository.REF)))
			{
				String[] alts = entity.getString(VcfRepository.ALT).split(",");
				if (entity.getString(VcfRepository.ALT).contains(","))
				{
					for(AttributeMetaData attributeMetaData : attributes){
						if (entity.getString(attributeMetaData.getName()).contains(",")){
							String[] values = entity.getString(attributeMetaData.getName()).split(",");
							for(int i = 0; i < alts.length; i++){
								alleleValueMap.put(alts[i],values[i]);
							}
						}
						StringBuilder newAttributeValue = new StringBuilder();
						String[] annotatedEntityAltAlleles = annotatedEntity.getString(VcfRepository.ALT).split(",");
						for(int i = 0; i < annotatedEntityAltAlleles.length; i++){
							if(i != 0){
								newAttributeValue.append(",");
							}
							newAttributeValue.append(alleleValueMap.get(annotatedEntityAltAlleles[i]));
						}
						entity.set(attributeMetaData.getName(),newAttributeValue.toString());
						processedResults.add(entity);
					}
				}
				else if (entity.get(VcfRepository.ALT).equals(annotatedEntity.get(VcfRepository.ALT)))
				{
					processedResults.add(entity);
				}
			}
		}
		return FluentIterable.from(processedResults).first();
	}
}
