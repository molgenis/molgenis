package org.molgenis.gavin.job.input.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;

import static org.molgenis.gavin.job.input.model.LineType.INDEL_NOCADD;
import static org.molgenis.gavin.job.input.model.LineType.VCF;

@AutoValue
public abstract class VcfVariant implements Variant
{
	public abstract String getChrom();

	public abstract long getPos();

	public abstract String getId();

	public abstract String getRef();

	public abstract String getAlt();

	@Override
	public LineType getLineType()
	{
		return isIndelWithoutCadd() ? INDEL_NOCADD : VCF;
	}

	@Override
	public String toString()
	{
		return Joiner.on('\t').join(getChrom(), getPos(), getId(), getRef(), getAlt(), '.', '.', '.');
	}

	private boolean isIndelWithoutCadd()
	{
		return getRef().length() > 1 || getAlt().length() > 1;
	}

	public static VcfVariant create(String chrom, long pos, String id, String ref, String alt)
	{
		return new AutoValue_VcfVariant(chrom, pos, id, ref, alt);
	}
}
