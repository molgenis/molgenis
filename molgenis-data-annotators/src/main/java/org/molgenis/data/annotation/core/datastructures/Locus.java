package org.molgenis.data.annotation.core.datastructures;

/**
 * Created by jvelde on 2/13/14.
 */
public class Locus
{

	String chrom;
	Integer pos;

	public Locus(String chrom, Integer pos)
	{
		this.chrom = chrom;
		this.pos = pos;
	}

	public String getChrom()
	{
		return chrom;
	}

	public Integer getPos()
	{
		return pos;
	}

	@Override
	public String toString()
	{
		return "Locus{" + "chrom='" + chrom + '\'' + ", pos=" + pos + '}';
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chrom == null) ? 0 : chrom.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Locus other = (Locus) obj;
		if (chrom == null)
		{
			if (other.chrom != null) return false;
		}
		else if (!chrom.equals(other.chrom)) return false;
		if (pos == null)
		{
			if (other.pos != null) return false;
		}
		else if (!pos.equals(other.pos)) return false;
		return true;
	}
}
