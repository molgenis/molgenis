package org.molgenis.data.annotation.query;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.model.VcfAttributes;

/**
 * Creates a Locus query that finds rows that match chromosome/position
 */
public class LocusQueryCreator implements QueryCreator
{
	private final VcfAttributes vcfAttributes;

	public LocusQueryCreator(VcfAttributes vcfAttributes)
	{

		this.vcfAttributes = requireNonNull(vcfAttributes);
	}

	@Override
	public Query<Entity> createQuery(Entity entity)
	{
		String chromosome = entity.getString(VcfAttributes.CHROM);
		Integer position = entity.getInt(VcfAttributes.POS);
		Locus locus = new Locus(chromosome, position);
		return createQuery(locus);
	}

	public static Query<Entity> createQuery(Locus locus)
	{
		return QueryImpl.EQ(VcfAttributes.CHROM, locus.getChrom()).and().eq(VcfAttributes.POS, locus.getPos());
	}

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(vcfAttributes.getChromAttribute(), vcfAttributes.getPosAttribute());
	}

}
