package org.molgenis.data.annotation.core.filter;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.entity.ResultFilter;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class ClinvarMultiAllelicResultFilter implements ResultFilter
{
	private final VcfAttributes vcfAttributes;

	public ClinvarMultiAllelicResultFilter(VcfAttributes vcfAttributes)
	{
		this.vcfAttributes = requireNonNull(vcfAttributes);
	}

	@Override
	public Collection<Attribute> getRequiredAttributes()
	{
		return Arrays.asList(vcfAttributes.getRefAttribute(), vcfAttributes.getAltAttribute());
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity, boolean updateMode)
	{
		if (updateMode == true)
		{
			throw new MolgenisDataException("This annotator/filter does not support updating of values");
		}
		Map<String, String> clnallValueMap = new LinkedHashMap<>();
		Map<String, String> clnsigValueMap = new LinkedHashMap<>();
		List<Entity> processedResults = new ArrayList<>();

		for (Entity entity : results)
		{
			if (entity.get(VcfAttributes.REF).equals(annotatedEntity.get(VcfAttributes.REF)))
			{
				String[] alts = entity.getString(VcfAttributes.ALT).split(",");
				String[] clnSigs = entity.getString("CLNSIG").split(",");
				String[] clnAll = entity.getString("CLNALLE").split(",");
				StringBuilder newClnlallAttributeValue = new StringBuilder();
				StringBuilder newClnlsigAttributeValue = new StringBuilder();
				String[] annotatedEntityAltAlleles = annotatedEntity.getString(VcfAttributes.ALT).split(",");
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

						String resultRefAllele = entity.getString(VcfAttributes.REF);
						String refAllele = annotatedEntity.getString(VcfAttributes.REF);

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
