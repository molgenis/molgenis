package org.molgenis.gavin.job.input.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;

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
		return VCF;
	}

	@Override
	public String toString()
	{
		return Joiner.on('\t').join(getChrom(), getPos(), getId(), getRef(), getAlt(), '.', '.', '.');
	}

	public static VcfVariant create(String chrom, long pos, String id, String ref, String alt)
	{
		return new AutoValue_VcfVariant(chrom, pos, id, ref, alt);
	}
}
