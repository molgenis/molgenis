package org.molgenis.data.annotation.core.query;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.core.datastructures.Locus;
import org.molgenis.data.annotation.core.entity.QueryCreator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

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
	public Collection<Attribute> getRequiredAttributes()
	{
		return Arrays.asList(vcfAttributes.getChromAttribute(), vcfAttributes.getPosAttribute());
	}

}
