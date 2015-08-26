package org.molgenis.data.annotation.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.vcf.VcfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

public class GoNLMultiAllelicResultFilter implements ResultFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(GoNLMultiAllelicResultFilter.class);
	private final List<AttributeMetaData> attributes;

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(VcfRepository.REF_META, VcfRepository.ALT_META);
	}

	public GoNLMultiAllelicResultFilter(List<AttributeMetaData> attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
	{
		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(attributes);
		List<Entity> processedResults = new ArrayList<>();
		Optional<Entity> filteredResult = multiAllelicResultFilter.filterResults(results, annotatedEntity);

		if (filteredResult.isPresent())
		{
			Entity entity = filteredResult.get();
			StringBuilder goNlAlleleFrequencyAttributeValue = new StringBuilder();

			if (entity.get(VcfRepository.REF).equals(annotatedEntity.get(VcfRepository.REF)))
			{
				String[] alts = annotatedEntity.getString(VcfRepository.ALT).split(",");
				String[] goNlAlleleCounts = entity.getString("INFO_AC").split(",");
				String goNlGenoTypeCounts = entity.getString("INFO_GTC");
				String[] goNlAlleleNumber = entity.getString("INFO_AN").split(",");

				for (int i = 0; i < alts.length; i++)
				{
						if(i != 0){
							goNlAlleleFrequencyAttributeValue.append(",");
						}
						goNlAlleleFrequencyAttributeValue.append(Double.parseDouble(goNlAlleleCounts[i])
								/ Double.parseDouble(goNlAlleleNumber[i]));
				}
				if (goNlAlleleFrequencyAttributeValue.toString().equals(""))
				{
					annotatedEntity.set("INFO_AF", "");
					annotatedEntity.set("INFO_GTC", "");
				}
				else
				{
					annotatedEntity.set("INFO_AF", goNlAlleleFrequencyAttributeValue.toString());
					annotatedEntity.set("INFO_GTC", goNlGenoTypeCounts);
				}
				processedResults.add(annotatedEntity);
			}
		}
		return FluentIterable.from(processedResults).first();
	}
}
