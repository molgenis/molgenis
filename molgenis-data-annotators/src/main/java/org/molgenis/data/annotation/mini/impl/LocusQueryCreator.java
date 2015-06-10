package org.molgenis.data.annotation.mini.impl;

import java.util.Arrays;
import java.util.Collection;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.mini.QueryCreator;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.VcfRepository;

/**
 * Creates a Locus query that finds rows that match chromosome/position
 */
public class LocusQueryCreator implements QueryCreator
{
	@Override
	public Query createQuery(Entity entity)
	{
		String chromosome = entity.getString(VcfRepository.CHROM);
		Long position = entity.getLong(VcfRepository.POS);
		Locus locus = new Locus(chromosome, position);
		return createQuery(locus);
	}

	public static Query createQuery(Locus locus)
	{
		return QueryImpl.EQ(VcfRepository.CHROM, locus.getChrom()).and().eq(VcfRepository.POS, locus.getPos());
	}

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(VcfRepository.CHROM_META, VcfRepository.POS_META);
	}

}
