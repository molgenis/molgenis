package org.molgenis.data.annotation.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.vcf.VcfRepository;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

public class ClinvarMultiAllelicResultFilter implements ResultFilter
{

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(VcfRepository.REF_META, VcfRepository.ALT_META);
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
	{
		Map<String, String> clnallValueMap = new LinkedHashMap<>();
		Map<String, String> clnsigValueMap = new LinkedHashMap<>();
		List<Entity> processedResults = new ArrayList<>();

		for (Entity entity : results)
		{
			if (entity.get(VcfRepository.REF).equals(annotatedEntity.get(VcfRepository.REF)))
			{
				String[] alts = entity.getString(VcfRepository.ALT).split(",");
				String[] clnSigs = entity.getString("CLNSIG").split(",");
				String[] clnAll = entity.getString("CLNALLE").split(",");
				StringBuilder newClnlallAttributeValue = new StringBuilder();
				StringBuilder newClnlsigAttributeValue = new StringBuilder();
				String[] annotatedEntityAltAlleles = annotatedEntity.getString(VcfRepository.ALT).split(",");
				// sometimes the clnsig is not defined for all alternative alleles
				// so we need to check this and just add what we have
				for (int i = 0; i < clnSigs.length; i++)
				{
					int significantAlleleIndex = Integer.parseInt(clnAll[i]);

					// this means the no allele is associated with the gene of interest
					if (significantAlleleIndex == -1) continue;

					// this means the allele is based on the reference
					else if (significantAlleleIndex == 0)
					{

						String resultRefAllele = entity.getString(VcfRepository.REF);
						String refAllele = annotatedEntity.getString(VcfRepository.REF);

						// if annotated entity allele equals the clinvar significant allele we want it!
						if (refAllele.equals(resultRefAllele))
						{
							// if more than one clinsigs are available pair the right one with each allele
							clnallValueMap.put(refAllele, "0");
							clnsigValueMap.put(refAllele, clnSigs[i]);

						}

					}
					// 1 based so we need subtract 1 from the clnAll value
					else
					{

						significantAlleleIndex = significantAlleleIndex - 1;

						for (int j = 0; j < annotatedEntityAltAlleles.length; j++)
						{

							if (annotatedEntityAltAlleles[j].equals(alts[significantAlleleIndex]))
							{

								String newSignificantAlleleIndex = Integer.toString(j + 1);

								clnallValueMap.put(alts[significantAlleleIndex], newSignificantAlleleIndex); // 1,2
								clnsigValueMap.put(alts[significantAlleleIndex], clnSigs[i]); // "5,4"

							}
						}
					}

				}

				for (int i = 0; i < annotatedEntityAltAlleles.length; i++)
				{
					if (i != 0)
					{
						newClnlallAttributeValue.append(",");
						newClnlsigAttributeValue.append(",");
					}
					if (clnallValueMap.get(annotatedEntityAltAlleles[i]) != null)
					{
						newClnlallAttributeValue.append(clnallValueMap.get(annotatedEntityAltAlleles[i]));
					}
					else
					{
						// missing allele in source, add a dot
						newClnlallAttributeValue.append(".");
					}

					if (clnsigValueMap.get(annotatedEntityAltAlleles[i]) != null)
					{
						newClnlsigAttributeValue.append(clnsigValueMap.get(annotatedEntityAltAlleles[i]));
					}
					else
					{
						// missing allele in source, add a dot
						newClnlsigAttributeValue.append(".");
					}
				}
				// nothing found at all? result is empty
				if (newClnlallAttributeValue.toString().equals("."))
				{
					entity.set("CLNSIG", "");
					entity.set("CLNALLE", "");
				}
				else
				{

					entity.set("CLNALLE", newClnlallAttributeValue.toString());
					entity.set("CLNSIG", newClnlsigAttributeValue.toString());

				}

				processedResults.add(entity);
			}

		}

		return FluentIterable.from(processedResults).first();
	}
}
