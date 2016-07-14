package org.molgenis.data.annotation.core.datastructures;

import com.google.auto.value.AutoValue;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.filter.MultiAllelicResultFilter;

import static org.molgenis.data.vcf.model.VcfAttributes.CHROM;
import static org.molgenis.data.vcf.model.VcfAttributes.POS;

/**
 * Used to compare equality of Chrom/Pos values in the {@link MultiAllelicResultFilter}.
 * <p>
 * Bit curious though: How come we've never needed this class more generally?
 */
@AutoValue
public abstract class Location
{
	public abstract String getChrom();

	public abstract int getPos();

	public static Location create(Entity entity)
	{
		return new AutoValue_Location(entity.getString(CHROM), entity.getInt(POS));
	}
}
