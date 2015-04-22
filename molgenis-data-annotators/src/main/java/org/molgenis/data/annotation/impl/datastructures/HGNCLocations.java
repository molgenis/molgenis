package org.molgenis.data.annotation.impl.datastructures;

/**
 * Created by jvelde on 1/30/14.
 */
public class HGNCLocations
{

	String hgnc;
	Long start;
	Long end;
	String chrom;

	public HGNCLocations(String hgnc, Long start, Long end, String chrom)
	{
		this.hgnc = hgnc;
		this.start = start;
		this.end = end;
		this.chrom = chrom;
	}

	@Override
	public String toString()
	{
		return "HgncLoc{" + "hgnc='" + hgnc + '\'' + ", start=" + start + ", end=" + end + ", chrom='" + chrom + '\''
				+ '}';
	}

	public String getHgnc()
	{
		return hgnc;
	}

	public Long getStart()
	{
		return start;
	}

	public Long getEnd()
	{
		return end;
	}

	public String getChrom()
	{
		return chrom;
	}
}
