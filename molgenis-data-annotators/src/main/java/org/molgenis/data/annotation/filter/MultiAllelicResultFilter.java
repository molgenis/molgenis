package org.molgenis.data.annotation.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.vcf.VcfRepository;

import com.google.common.collect.FluentIterable;

/**
 * 
 * TODO: Support multi-allelic combination fields. These fields contain not only info for ref-alt pairs, but also values
 * for all possible alt-alt pairs. For example, the "AC_Het" field in ExAC:
 * 
 * For 2 alt alleles, there are 3 values in the AC_Het field, the third being the count for T-A: 1 6536051 . C T,A
 * 11129.51 PASS AC_Adj=5,3;AC_Het=5,3,0;AC_Hom=0,0; Alt-allele combinations in AC_Het field occur in the following
 * order: C-T, C-A, T-A.
 * 
 * For 3 alt alleles, there are 6 values (3+2+1): 15 66641732 rs2063690 G C,A,T 35371281.87 PASS
 * AC_Adj=13570,3,2;AC_Het=11380,1,2,2,0,0;AC_Hom=1094,0,0; Alt-allele combinations in AC_Het field occur in the
 * following order: G-C, G-A, G-T, C-A, C-T, A-T.
 * 
 * For 4 alt alleles, there are 10 values (4+3+2+1): 21 45650009 rs3831401 T G,C,TG,A 8366813.26 PASS
 * AC_Adj=2528,3415,1,0;AC_Het=934,1240,0,0,725,1,0,0,0,0;AC_Hom=434,725,0,0; Alt-allele combinations in AC_Het field
 * occur in the following order: T-G, T-C, T-TG, T-A, G-C, G-TG, G-A, C-TG, C-A, TG-A.
 *
 *
 *
 * TODO: Smart matching of non-obvious ref and alt alleles. Right now, we only 'string match' the exact values of ref
 * and alt alleles. However, depending on which other genotypes you call for a certain genomic position, the same
 * variant may be denoted in a different way. For example: "1 231094050 GA G" is the same variant as the GAA/GA in
 * "1 231094050 GAA GA,G", but to allow notation of the the AA deletion (in GAA/G), the ref was written as GAA instead
 * of GA.
 * 
 * This can be tricky. Consider this variant: 1 6529182 . TTCCTCC TTCC
 * 
 * And after some puzzling, you will find that it is seen in ExAC: 1 6529182 . TTCCTCCTCC
 * TTCCTCC,TTCC,T,TTCCTCCTCCTCC,TTCCTCCTCCTCCTCC,TTCCTCCTCCTCCTCCTCCTCC
 * 
 * But here denoted as "TTCCTCCTCC/TTCCTCC". In both cases, a TCC was deleted, but in ExAC this variant is trailed with
 * another TCC. Finding and parsing these variants to correctly match them against databases such as 1000 Genomes and
 * ExAC would be very valuable.
 * 
 *
 */
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
	public com.google.common.base.Optional<Entity> filterResults(Iterable<Entity> resourceEntities, Entity sourceEntity)
	{
		List<Entity> processedResults = new ArrayList<>();

		for (Entity resourceEntity : resourceEntities)
		{
			if (resourceEntity.get(VcfRepository.REF).equals(sourceEntity.get(VcfRepository.REF)))
			{
				filter(sourceEntity, processedResults, resourceEntity, "", "");
			}
			else if(resourceEntity.getString(VcfRepository.REF).indexOf(sourceEntity.getString(VcfRepository.REF))==0) {
				String postFix = resourceEntity.getString(VcfRepository.REF).substring(sourceEntity.getString(VcfRepository.REF).length());
				filter(sourceEntity, processedResults, resourceEntity, postFix, "");
			}
			else if(sourceEntity.getString(VcfRepository.REF).indexOf(resourceEntity.getString(VcfRepository.REF))==0) {
				String postFix = sourceEntity.getString(VcfRepository.REF).substring(resourceEntity.getString(VcfRepository.REF).length());
				filter(sourceEntity, processedResults, resourceEntity, "", postFix);
			}
		}
		return FluentIterable.from(processedResults).first();
	}

	private void filter(Entity annotatedEntity, List<Entity> processedResults, Entity entity, String sourcePostfix, String resourcePostfix) {
		Map<String, String> alleleValueMap = new HashMap<>();
		String[] alts = entity.getString(VcfRepository.ALT).split(",");

		for (AttributeMetaData attributeMetaData : attributes)
        {
            String[] values = entity.getString(attributeMetaData.getName()).split(",");
            for (int i = 0; i < alts.length; i++)
            {
                alleleValueMap.put(alts[i]+resourcePostfix, values[i]);
            }
            StringBuilder newAttributeValue = new StringBuilder();
            String[] annotatedEntityAltAlleles = annotatedEntity.getString(VcfRepository.ALT).split(",");
            for (int i = 0; i < annotatedEntityAltAlleles.length; i++)
            {
                if (i != 0)
                {
                    newAttributeValue.append(",");
                }
                if (alleleValueMap.get(annotatedEntityAltAlleles[i]+sourcePostfix) != null)
                {
                    newAttributeValue.append(alleleValueMap.get(annotatedEntityAltAlleles[i]+sourcePostfix));
                }
                else
                {
                    // missing allele in source, add a dot
                    newAttributeValue.append(".");
                }
            }
            // add entity only if something was found
            if (!newAttributeValue.toString().equals("."))
            {
                entity.set(attributeMetaData.getName(), newAttributeValue.toString());
                processedResults.add(entity);
            }
        }
	}
}
