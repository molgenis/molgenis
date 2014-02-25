package org.molgenis.data.annotation.impl.datastructures;

/**
 * Created with IntelliJ IDEA. User: charbonb Date: 24/02/14 Time: 12:32 To change this template use File | Settings |
 * File Templates.
 */
public class CosmicData
{
	String ID;
	String feature_type;
	String[] alt_alleles;
	Integer end;
	String seq_region_name;
	String consequence_type;
	int strand;
	Integer start;

	public String getID()
	{
		return ID;
	}

	public void setID(String ID)
	{
		this.ID = ID;
	}

	public String getFeature_type()
	{
		return feature_type;
	}

	public void setFeature_type(String feature_type)
	{
		this.feature_type = feature_type;
	}

	public String[] getAlt_alleles()
	{
		return alt_alleles;
	}

	public void setAlt_alleles(String[] alt_alleles)
	{
		this.alt_alleles = alt_alleles;
	}

	public Integer getEnd()
	{
		return end;
	}

	public void setEnd(Integer end)
	{
		this.end = end;
	}

	public String getSeq_region_name()
	{
		return seq_region_name;
	}

	public void setSeq_region_name(String seq_region_name)
	{
		this.seq_region_name = seq_region_name;
	}

	public String getConsequence_type()
	{
		return consequence_type;
	}

	public void setConsequence_type(String consequence_type)
	{
		this.consequence_type = consequence_type;
	}

	public int getStrand()
	{
		return strand;
	}

	public void setStrand(int strand)
	{
		this.strand = strand;
	}

	public Integer getStart()
	{
		return start;
	}

	public void setStart(Integer start)
	{
		this.start = start;
	}
}
