package org.molgenis.data.annotation.filter;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;


public class GoNLMultiAllelicResultFilter implements ResultFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(GoNLMultiAllelicResultFilter.class);
	private List<AttributeMetaData> attributes;

	public GoNLMultiAllelicResultFilter(List<AttributeMetaData> attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(VcfRepository.REF_META, VcfRepository.ALT_META);
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
	{
		Map<String, Double> goNlAlleleFrequenciesMap = new HashMap<>();
		Map<String, String> goNlGenoTypeCountsMap = new HashMap<>();
		List<Entity> processedResults = new ArrayList<>();

		for (Entity entity : results)
		{		
			
			StringBuilder goNlAlleleFrequencyAttributeValue = new StringBuilder();
			StringBuilder goNlGtcAttributeValue = new StringBuilder();

			if (entity.get(VcfRepository.REF).equals(annotatedEntity.get(VcfRepository.REF)))
			{
				String[] alts = entity.getString(VcfRepository.ALT).split(",");
				String goNlAlleleCounts = entity.getString("INFO_AC");
				String goNlGenoTypeCounts = entity.getString("INFO_GTC");
				String goNlAlleleNumber = entity.getString("INFO_AN");
				double goNlAlleleFrequency = 0;

				String[] annotatedEntityAltAlleles = annotatedEntity.getString(VcfRepository.ALT).split(",");
				if (annotatedEntityAltAlleles.length > 1)
				{
					LOG.error("More than one alternative allele found, caution GTC value not reliable for entry: "
							+ entity.getString(VcfRepository.CHROM) + ":" + entity.getString(VcfRepository.POS)
							+ " ref: " + entity.getString(VcfRepository.REF) + " alt:"
							+ entity.getString(VcfRepository.ALT));
				}
				for (String alt : alts)
				{
					for (String annotatedEntityAltAllele : annotatedEntityAltAlleles)
					{
						if (alt.equals(annotatedEntityAltAllele))
						{
							goNlAlleleFrequency = Double.parseDouble(goNlAlleleCounts)
									/ Double.parseDouble(goNlAlleleNumber);
							goNlGenoTypeCountsMap.put(alt, goNlGenoTypeCounts);
							goNlAlleleFrequenciesMap.put(alt, goNlAlleleFrequency);
						}

					}
				}
				for (int i = 0; i < annotatedEntityAltAlleles.length; i++)
				{
					if (i != 0)
					{
						goNlAlleleFrequencyAttributeValue.append(",");
					}
					if (goNlGenoTypeCountsMap.get(annotatedEntityAltAlleles[i]) != null)
					{
						goNlGtcAttributeValue.append(goNlGenoTypeCountsMap
								.get(annotatedEntityAltAlleles[i]));
					}
					else
					{
						// missing allele in source, add a dot
						goNlAlleleFrequencyAttributeValue.append(".");
					}
					if (goNlAlleleFrequenciesMap.get(annotatedEntityAltAlleles[i]) != null)
					{
						goNlAlleleFrequencyAttributeValue.append(goNlAlleleFrequenciesMap.get(annotatedEntityAltAlleles[i]));
					}
					else
					{
						// missing allele in source, add a dot
						goNlGtcAttributeValue.append(".");
					}
				}
				// nothing found at all? result is empty
				DefaultEntityMetaData repoMetaData = new DefaultEntityMetaData("GoNlResultEntity");
				repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("INFO_AF", DECIMAL));	
				repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("INFO_GTC", FieldTypeEnum.STRING));
				repoMetaData.addAttribute("id").setIdAttribute(true).setVisible(false);
				
				Entity goNlResultEntity = new MapEntity(repoMetaData); 
				
				if (goNlAlleleFrequencyAttributeValue.toString().equals("."))
				{
					goNlResultEntity.set("INFO_GoNL_AF", "");
					goNlResultEntity.set("INFO_GoNL_GTC", "");
				}
				else
				{				
					goNlResultEntity.set("INFO_AF", goNlAlleleFrequencyAttributeValue.toString());
					goNlResultEntity.set("INFO_GTC", goNlGtcAttributeValue.toString());

				}
				processedResults.add(goNlResultEntity);
			}

		}
		return FluentIterable.from(processedResults).first();
	}
}
