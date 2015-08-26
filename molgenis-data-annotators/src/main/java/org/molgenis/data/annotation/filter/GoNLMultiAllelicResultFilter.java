package org.molgenis.data.annotation.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.annotation.entity.impl.GoNLAnnotator;
import org.molgenis.data.vcf.VcfRepository;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

public class GoNLMultiAllelicResultFilter implements ResultFilter
{
	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(VcfRepository.REF_META, VcfRepository.ALT_META);
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
	{
		List<Entity> processedResults = new ArrayList<>();

		for (Entity entity : results)
		{
			Double goNlAlleleFrequency;

			if ((entity.get(VcfRepository.REF).equals(annotatedEntity.get(VcfRepository.REF))))
			{
				String[] alts = annotatedEntity.getString(VcfRepository.ALT).split(",");
				String goNlAlleleCounts = entity.getString(GoNLAnnotator.INFO_AC);
				String goNlGenoTypeCounts = entity.getString(GoNLAnnotator.INFO_GTC);
				String goNlAlleleNumber = entity.getString(GoNLAnnotator.INFO_AN);

				for (int i = 0; i < alts.length; i++)
				{
					if (alts[i].equals(entity.getString(VcfRepository.ALT)))
					{

						goNlAlleleFrequency = (Double.parseDouble(goNlAlleleCounts) / Double
								.parseDouble(goNlAlleleNumber));
						annotatedEntity.set(GoNLAnnotator.INFO_AF, goNlAlleleFrequency.toString());
						annotatedEntity.set(GoNLAnnotator.INFO_GTC, goNlGenoTypeCounts);
					}
				}

				processedResults.add(annotatedEntity);
			}
		}
		return FluentIterable.from(processedResults).first();
	}
}
