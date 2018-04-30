package org.molgenis.gavin.job.input.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.annotation.core.entity.impl.CaddAnnotator;

import javax.annotation.Nullable;

import static org.molgenis.gavin.job.input.model.LineType.CADD;

@AutoValue
public abstract class CaddVariant implements Variant
{
	public abstract String getChrom();

	public abstract long getPos();

	public abstract String getRef();

	public abstract String getAlt();

	@Nullable
	public abstract Double getRawScore();

	@Nullable
	public abstract Double getPhred();

	public String getInfo()
	{
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		if (getRawScore() != null)
		{
			builder.put(CaddAnnotator.CADD_ABS, getRawScore().toString());
		}
		if (getPhred() != null)
		{
			builder.put(CaddAnnotator.CADD_SCALED, getPhred().toString());
		}
		return Joiner.on(";").withKeyValueSeparator("=").join(builder.build());
		//		return "CADD=" + getRawScore() + ";CADD_SCALED=" + getPhred();
	}

	@Override
	public LineType getLineType()
	{
		return CADD;
	}

	public static CaddVariant create(String chrom, long pos, String ref, String alt, Double rawScore, Double phred)
	{
		return new AutoValue_CaddVariant(chrom, pos, ref, alt, rawScore, phred);
	}

	@Override
	public String toString()
	{
		return Joiner.on('\t').join(getChrom(), getPos(), '.', getRef(), getAlt(), '.', '.', getInfo());
	}
}
