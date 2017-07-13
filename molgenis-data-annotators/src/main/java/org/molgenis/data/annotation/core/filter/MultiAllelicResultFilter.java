package org.molgenis.data.annotation.core.filter;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.datastructures.Location;
import org.molgenis.data.annotation.core.entity.ResultFilter;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.util.*;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.molgenis.data.vcf.model.VcfAttributes.ALT;
import static org.molgenis.data.vcf.model.VcfAttributes.REF;

/**
 * TODO: Support multi-allelic combination fields. These fields contain not only info for ref-alt pairs, but also values
 * for all possible alt-alt pairs. For example, the "AC_Het" field in ExAC:
 * <p>
 * For 2 alt alleles, there are 3 values in the AC_Het field, the third being the count for T-A: 1 6536051 . C T,A
 * 11129.51 PASS AC_Adj=5,3;AC_Het=5,3,0;AC_Hom=0,0; Alt-allele combinations in AC_Het field occur in the following
 * order: C-T, C-A, T-A.
 * <p>
 * For 3 alt alleles, there are 6 values (3+2+1): 15 66641732 rs2063690 G C,A,T 35371281.87 PASS
 * AC_Adj=13570,3,2;AC_Het=11380,1,2,2,0,0;AC_Hom=1094,0,0; Alt-allele combinations in AC_Het field occur in the
 * following order: G-C, G-A, G-T, C-A, C-T, A-T.
 * <p>
 * For 4 alt alleles, there are 10 values (4+3+2+1): 21 45650009 rs3831401 T G,C,TG,A 8366813.26 PASS
 * AC_Adj=2528,3415,1,0;AC_Het=934,1240,0,0,725,1,0,0,0,0;AC_Hom=434,725,0,0; Alt-allele combinations in AC_Het field
 * occur in the following order: T-G, T-C, T-TG, T-A, G-C, G-TG, G-A, C-TG, C-A, TG-A.
 * <p>
 * <p>
 * <p>
 * TODO: Smart matching of non-obvious ref and alt alleles. Right now, we only 'string match' the exact values of ref
 * and alt alleles. However, depending on which other genotypes you call for a certain genomic position, the same
 * variant may be denoted in a different way. For example: "1 231094050 GA G" is the same variant as the GAA/GA in
 * "1 231094050 GAA GA,G", but to allow notation of the the AA deletion (in GAA/G), the ref was written as GAA instead
 * of GA.
 * <p>
 * This can be tricky. Consider this variant: 1 6529182 . TTCCTCC TTCC
 * <p>
 * And after some puzzling, you will find that it is seen in ExAC: 1 6529182 . TTCCTCCTCC
 * TTCCTCC,TTCC,T,TTCCTCCTCCTCC,TTCCTCCTCCTCCTCC,TTCCTCCTCCTCCTCCTCCTCC
 * <p>
 * But here denoted as "TTCCTCCTCC/TTCCTCC". In both cases, a TCC was deleted, but in ExAC this variant is trailed with
 * another TCC. Finding and parsing these variants to correctly match them against databases such as 1000 Genomes and
 * ExAC would be very valuable.
 */
public class MultiAllelicResultFilter implements ResultFilter
{
	private final List<Attribute> attributes;
	private final boolean mergeMultilineResourceResults;
	private final VcfAttributes vcfAttributes;

	public MultiAllelicResultFilter(List<Attribute> alleleSpecificAttributes, boolean mergeMultilineResourceResults,
			VcfAttributes vcfAttributes)
	{
		this.attributes = alleleSpecificAttributes;
		this.mergeMultilineResourceResults = mergeMultilineResourceResults;
		this.vcfAttributes = vcfAttributes;
	}

	public MultiAllelicResultFilter(List<Attribute> alleleSpecificAttributes, VcfAttributes vcfAttributes)
	{
		this.attributes = alleleSpecificAttributes;
		this.mergeMultilineResourceResults = false;
		this.vcfAttributes = vcfAttributes;
	}

	@Override
	public Collection<Attribute> getRequiredAttributes()
	{
		return asList(vcfAttributes.getRefAttribute(), vcfAttributes.getAltAttribute());
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> resourceEntities, Entity sourceEntity, boolean updateMode)
	{
		List<Entity> processedResults = new ArrayList<>();

		String sourceRef = sourceEntity.getString(REF);
		if (sourceRef == null)
		{
			return Optional.absent();
		}

		if (mergeMultilineResourceResults)
		{
			resourceEntities = merge(resourceEntities);
		}

		for (Entity resourceEntity : resourceEntities)
		{
			String resourceRef = resourceEntity.getString(REF);

			if (resourceRef.equals(sourceRef))
			{
				processedResults.addAll(filter(sourceEntity, resourceEntity, "", "", updateMode));
			}
			// example: ref AGG, input A, substring AGG from index 1, so GG is the postfix to use to match against this
			// reference
			else if (resourceRef.startsWith(sourceRef))
			{
				String postFix = resourceRef.substring(sourceRef.length());
				processedResults.addAll(filter(sourceEntity, resourceEntity, postFix, "", updateMode));
			}
			// example: ref T, input TC, substring TC from index 1, so C is the postfix to use to match against this
			// input
			else if (sourceRef.startsWith(resourceRef))
			{
				String postFix = sourceRef.substring(resourceRef.length());
				processedResults.addAll(filter(sourceEntity, resourceEntity, "", postFix, updateMode));
			}
		}
		return from(processedResults).first();
	}

	private List<Entity> filter(Entity sourceEntity, Entity resourceEntity, String sourcePostfix,
			String resourcePostfix, boolean updateMode)
	{
		List<Entity> result = Lists.newArrayList();
		Map<String, String> alleleValueMap = new HashMap<>();
		Map<String, String> sourceAlleleValueMap = new HashMap<>();
		String[] alts = resourceEntity.getString(VcfAttributes.ALT).split(",");
		String[] sourceAlts = sourceEntity.getString(VcfAttributes.ALT).split(",");
		for (Attribute attribute : attributes)
		{
			String[] values = resourceEntity.getString(attribute.getName()).split(",", -1);
			for (int i = 0; i < alts.length; i++)
			{
				alleleValueMap.put(alts[i] + resourcePostfix, values[i]);
			}

			// also compile a list of original source alleles and their values for use in 'update mode'
			if (updateMode && sourceEntity.get(attribute.getName()) != null)
			{
				Attribute sourceAttr = sourceEntity.getEntityType().getAttribute(attribute.getName());
				if (EntityTypeUtils.isTextType(sourceAttr) || EntityTypeUtils.isStringType(sourceAttr))
				{
					String[] sourceValues = sourceEntity.getString(attribute.getName()).split(",", -1);
					for (int i = 0; i < sourceAlts.length; i++)
					{
						sourceAlleleValueMap.put(sourceAlts[i] + resourcePostfix,
								sourceValues[i].isEmpty() ? "." : sourceValues[i]);
					}
				}
				else if (sourceAlts.length == 1)
				{
					sourceAlleleValueMap.put(sourceAlts[0], sourceEntity.get(attribute.getName()).toString());
				}
			}

			StringBuilder newAttributeValue = new StringBuilder();
			String[] annotatedEntityAltAlleles = sourceEntity.getString(ALT).split(",");
			for (int i = 0; i < annotatedEntityAltAlleles.length; i++)
			{
				if (i != 0)
				{
					newAttributeValue.append(",");
				}
				if (alleleValueMap.get(annotatedEntityAltAlleles[i] + sourcePostfix) != null)
				{
					newAttributeValue.append(alleleValueMap.get(annotatedEntityAltAlleles[i] + sourcePostfix));
				}
				else
				{
					// default: no data in in resource, add "." for missing value
					// because we are not in update mode, don't check the original source
					if (updateMode == false)
					{
						newAttributeValue.append(".");
					}
					else
					{
						// we are in update mode, so let's check the source entity if there was an original value
						if (sourceAlleleValueMap.get(annotatedEntityAltAlleles[i] + sourcePostfix) != null)
						{
							newAttributeValue.append(
									sourceAlleleValueMap.get(annotatedEntityAltAlleles[i] + sourcePostfix));
						}
						// if there was no original value either, add "." for missing value
						else
						{
							newAttributeValue.append(".");
						}
					}
				}
			}
			// add entity only if something was found, so no '.' or any multiple of '.,' (e.g. ".,.,.")
			if (!newAttributeValue.toString().matches("[\\.,]+"))
			{
				resourceEntity.set(attribute.getName(), newAttributeValue.toString());
				result.add(resourceEntity);
			}
		}
		return result;
	}

	/**
	 * Combine ALT information per reference allele (in VCF there is only 1 reference by letting ALT vary, but that
	 * might not always be the case)
	 * <p>
	 * So we want to support this hypothetical example:
	 * 3	300	G	A	0.2|23.1
	 * 3	300	G	T	-2.4|0.123
	 * 3	300	G	X	-0.002|2.3
	 * 3	300	G	C	0.5|14.5
	 * 3	300	GC	A	0.2|23.1
	 * 3	300	GC	T	-2.4|0.123
	 * 3	300	C	GX	-0.002|2.3
	 * 3	300	C	GC	0.5|14.5
	 * <p>
	 * <p>
	 * So we want to support this hypothetical example: 3 300 G A 0.2|23.1 3 300 G T -2.4|0.123 3 300 G X -0.002|2.3 3
	 * 300 G C 0.5|14.5 3 300 GC A 0.2|23.1 3 300 GC T -2.4|0.123 3 300 C GX -0.002|2.3 3 300 C GC 0.5|14.5
	 * <p>
	 * and it should become:
	 * <p>
	 * 3 300 G A,T,X,C 0.2|23.1,-2.4|0.123,-0.002|2.3,0.5|14.5 3 300 GC A,T 0.2|23.1,-2.4|0.123 3 300 C GX,GC
	 * -0.002|2.3,0.5|14.5
	 * <p>
	 * <p>
	 * 3	300	G	A,T,X,C	0.2|23.1,-2.4|0.123,-0.002|2.3,0.5|14.5
	 * 3	300	GC	A,T	0.2|23.1,-2.4|0.123
	 * 3	300	C	GX,GC	-0.002|2.3,0.5|14.5
	 * <p>
	 * so that the multi-allelic filter can then find back the appropriate values as if it were a multi-allelic VCF line
	 */
	public Iterable<Entity> merge(Iterable<Entity> resourceEntities)
	{
		ArrayList<Entity> resourceEntitiesMerged = new ArrayList<>();

		PeekingIterator<Entity> resourceEntitiesIterator = Iterators.peekingIterator(resourceEntities.iterator());

		if (!resourceEntitiesIterator.hasNext())
		{
			return resourceEntitiesMerged;
		}
		Location location = Location.create(resourceEntitiesIterator.peek());

		// collect entities to be merged by ref
		Multimap<String, Entity> refToMergedEntity = LinkedListMultimap.create();

		while (resourceEntitiesIterator.hasNext())
		{
			Entity resourceEntity = resourceEntitiesIterator.next();
			// verify if all results have the same chrom & pos
			Location thisLoc = Location.create(resourceEntity);

			// at least chrom and pos have to be the same, ref may be different
			if (!location.equals(thisLoc))
			{
				throw new MolgenisDataException("Mismatch in location! " + location + " vs " + thisLoc);
			}

			// add to map by ref, so we get [ref -> entities to be merged into one]
			refToMergedEntity.put(resourceEntity.getString(REF), resourceEntity);
		}

		// now iterate over map with refs and merge entities per ref
		for (String refKey : refToMergedEntity.keySet())
		{
			boolean first = true;
			Entity mergeWithMe = null;
			for (Entity entityToBeMerged : refToMergedEntity.get(refKey))
			{
				if (first)
				{
					// merge all following entities with the first one
					mergeWithMe = entityToBeMerged;
					first = false;
				}
				else
				{
					// concatenate alleles
					mergeWithMe.set(ALT, mergeWithMe.get(ALT).toString() + "," + entityToBeMerged.get(ALT).toString());

					// concatenate allele specific attributes
					for (Attribute alleleSpecificAttributes : attributes)
					{
						String attrName = alleleSpecificAttributes.getName();
						mergeWithMe.set(attrName,
								mergeWithMe.get(attrName).toString() + "," + entityToBeMerged.get(attrName).toString());
					}
				}
			}
			resourceEntitiesMerged.add(mergeWithMe);
		}
		return resourceEntitiesMerged;
	}
}
