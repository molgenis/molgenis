package org.molgenis.data.annotation.filter;

import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.POS;

import org.molgenis.data.Entity;
import com.google.auto.value.AutoValue;

/**
 * Used to compare equality of Chrom/Pos values in the {@link MultiAllelicResultFilter}.
 * 
 * Bit curious though: How come we've never needed this class more generally?
 */
@AutoValue
abstract class Location
{
	public abstract String getChrom();

	public abstract long getPos();

	public static Location create(Entity entity)
	{
		return new AutoValue_Location(entity.getString(CHROM), entity.getLong(POS));
	}
}
